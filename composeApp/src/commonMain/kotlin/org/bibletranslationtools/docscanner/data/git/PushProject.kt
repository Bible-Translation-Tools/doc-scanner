package org.bibletranslationtools.docscanner.data.git

import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.git_awaiting_report
import docscanner.composeapp.generated.resources.git_non_existing
import docscanner.composeapp.generated.resources.git_not_attempted
import docscanner.composeapp.generated.resources.git_ok
import docscanner.composeapp.generated.resources.git_rejected_non_delete
import docscanner.composeapp.generated.resources.git_rejected_non_fast_forward
import docscanner.composeapp.generated.resources.git_rejected_other_reason
import docscanner.composeapp.generated.resources.git_rejected_other_reason_detailed
import docscanner.composeapp.generated.resources.git_rejected_remote_changed
import docscanner.composeapp.generated.resources.git_server_details
import docscanner.composeapp.generated.resources.git_up_to_date
import docscanner.composeapp.generated.resources.pref_default_git_server_port
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bibletranslationtools.docscanner.OnProgressListener
import org.bibletranslationtools.docscanner.data.Settings
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.jetbrains.compose.resources.getString
import org.unfoldingword.gogsclient.User
import java.io.IOException

class PushProject(
    private val getRepository: GetRepository,
    private val prefRepo: PreferenceRepository,
    private val directoryProvider: DirectoryProvider
) {
    data class Result(
        val status: Status,
        val message: String?
    )

    private val max = 100
    private val logger = KotlinLogging.logger {}

    suspend fun execute(
        project: Project,
        user: User,
        progressListener: OnProgressListener? = null
    ): Result {
        val repository = getRepository.execute(project, user, progressListener)
        try {
            val repo: Repo = project.getRepo(directoryProvider)
            repo.commit()

            return push(repo, repository!!.sshUrl, progressListener)
        } catch (e: Exception) {
            logger.error(e) { "Error pushing project ${project.getName()}" }
        }

        return Result(Status.UNKNOWN, null)
    }

    @Throws(JGitInternalException::class)
    private suspend fun push(repo: Repo, remote: String, progressListener: OnProgressListener?): Result {
        progressListener?.onProgress(-1, max, "Uploading translation")

        var status = Status.UNKNOWN
        val git: Git
        try {
            repo.deleteRemote("origin")
            repo.setRemote("origin", remote)
            git = repo.getGit()
        } catch (e: IOException) {
            return Result(status, e.message)
        }

        val spec = RefSpec("refs/heads/master")

        // TODO: we might want to get some progress feedback for the user
        val port = prefRepo.getPref(
            Settings.KEY_PREF_GIT_SERVER_PORT,
            getString(Res.string.pref_default_git_server_port)
        ).toInt()
        val pushCommand = git.push()
            .setTransportConfigCallback(TransportCallback(directoryProvider, port))
            .setRemote("origin")
            .setPushTags()
            .setForce(false)
            .setRefSpecs(spec)

        try {
            val pushResults = pushCommand.call()
            val response = StringBuilder()
            status = Status.OK // will be OK if no errors are found
            for (r in pushResults) {
                val updates = r.remoteUpdates
                for (update in updates) {
                    response.append(parseRemoteRefUpdate(update, remote))
                    response.append("\n")

                    val updateStatus = update.status
                    when (updateStatus) {
                        RemoteRefUpdate.Status.OK, RemoteRefUpdate.Status.UP_TO_DATE -> {}
                        RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD -> {
                            status = Status.REJECTED_NON_FAST_FORWARD
                        }
                        RemoteRefUpdate.Status.REJECTED_NODELETE -> {
                            status = Status.REJECTED_NODELETE
                        }
                        RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED -> {
                            status = Status.REJECTED_REMOTE_CHANGED
                        }
                        RemoteRefUpdate.Status.REJECTED_OTHER_REASON -> {
                            status = Status.REJECTED_OTHER_REASON
                        }
                        else -> status = Status.UNKNOWN
                    }
                }

                if (status.isRejected) {
                    // pushRejectedResults = r // save rejection data
                }
            }
            // give back the response message
            return Result(status, response.toString())
        } catch (e: TransportException) {
            logger.error(e) { "Error pushing project" }
            val cause = e.cause
            if (cause != null) {
                val subException = cause.cause
                if (subException != null) {
                    val detail = subException.message
                    if ("Auth fail" == detail) {
                        status = Status.AUTH_FAILURE
                    }
                } else if (cause is NoRemoteRepositoryException) {
                    status = Status.NO_REMOTE_REPO
                } else if (cause.message!!.contains("not permitted")) {
                    status = Status.AUTH_FAILURE
                }
            }
            return Result(status, null)
        } catch (e: OutOfMemoryError) {
            logger.error(e) { "Error pushing project" }
            status = Status.OUT_OF_MEMORY
            return Result(status, null)
        } catch (e: java.lang.Exception) {
            logger.error(e) { "Error pushing project" }
            return Result(status, null)
        } catch (e: Throwable) {
            logger.error(e) { "Error pushing project" }
            return Result(status, null)
        }
    }

    enum class Status {
        OK,
        OUT_OF_MEMORY,
        AUTH_FAILURE,
        NO_REMOTE_REPO,
        REJECTED_NON_FAST_FORWARD,
        REJECTED_NODELETE,
        REJECTED_OTHER_REASON,
        REJECTED_REMOTE_CHANGED,
        UNKNOWN;

        val isRejected: Boolean
            get() = this == REJECTED_NON_FAST_FORWARD ||
                    this == REJECTED_NODELETE ||
                    this == REJECTED_OTHER_REASON ||
                    this == REJECTED_REMOTE_CHANGED
    }

    /**
     * Parses the response from the remote
     * @param update
     * @return
     */
    private suspend fun parseRemoteRefUpdate(update: RemoteRefUpdate, remote: String): String {
        var msg: String?
        when (update.status) {
            RemoteRefUpdate.Status.AWAITING_REPORT -> {
                msg = String.format(
                    getString(Res.string.git_awaiting_report), update.remoteName
                )
            }
            RemoteRefUpdate.Status.NON_EXISTING -> {
                msg = String.format(
                    getString(Res.string.git_non_existing),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.NOT_ATTEMPTED -> {
                msg = String.format(
                    getString(Res.string.git_not_attempted),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.OK -> {
                msg = String.format(
                    getString(Res.string.git_ok),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_NODELETE -> {
                msg = String.format(
                    getString(Res.string.git_rejected_non_delete),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD -> {
                msg = String.format(
                    getString(Res.string.git_rejected_non_fast_forward),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_OTHER_REASON -> {
                val reason = update.message
                msg = if (reason.isNullOrEmpty()) {
                    String.format(
                        getString(Res.string.git_rejected_other_reason),
                        update.remoteName
                    )
                } else {
                    String.format(
                        getString(Res.string.git_rejected_other_reason_detailed),
                        update.remoteName,
                        reason
                    )
                }
            }
            RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED -> {
                msg = String.format(
                    getString(Res.string.git_rejected_remote_changed),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.UP_TO_DATE -> {
                msg = String.format(
                    getString(Res.string.git_up_to_date),
                    update.remoteName
                )
            }
            else -> msg = "Unknown status"
        }
        msg += "\n" + String.format(
            getString(Res.string.git_server_details),
            remote
        )
        return msg
    }
}