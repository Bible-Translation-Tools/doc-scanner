package org.bibletranslationtools.docscanner.data.local.git

import android.content.Context
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import java.io.IOException

class PushProject(
    private val context: Context,
    private val getRepository: GetRepository,
    private val prefRepo: PreferenceRepository,
    private val directoryProvider: DirectoryProvider
) {
    data class Result(
        val status: Status,
        val message: String?
    )

    private val max = 100

    fun execute(
        project: Project,
        profile: Profile,
        progressListener: OnProgressListener? = null
    ): Result {
        if (profile.gogsUser != null) {
            val repository = getRepository.execute(project, profile, progressListener)
            try {
                val repo: Repo = project.getRepo(directoryProvider)
                repo.commit()

                return push(repo, repository!!.sshUrl, progressListener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            return Result(Status.AUTH_FAILURE, null)
        }

        return Result(Status.UNKNOWN, null)
    }

    @Throws(JGitInternalException::class)
    private fun push(repo: Repo, remote: String, progressListener: OnProgressListener?): Result {
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
            context.getString(R.string.pref_default_git_server_port)
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
            e.printStackTrace()
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
            e.printStackTrace()
            status = Status.OUT_OF_MEMORY
            return Result(status, null)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return Result(status, null)
        } catch (e: Throwable) {
            e.printStackTrace()
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
    private fun parseRemoteRefUpdate(update: RemoteRefUpdate, remote: String): String {
        var msg: String?
        when (update.status) {
            RemoteRefUpdate.Status.AWAITING_REPORT -> {
                msg = String.format(
                    context.resources.getString(R.string.git_awaiting_report), update.remoteName
                )
            }
            RemoteRefUpdate.Status.NON_EXISTING -> {
                msg = String.format(
                    context.resources.getString(R.string.git_non_existing),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.NOT_ATTEMPTED -> {
                msg = String.format(
                    context.resources.getString(R.string.git_not_attempted),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.OK -> {
                msg = String.format(
                    context.resources.getString(R.string.git_ok),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_NODELETE -> {
                msg = String.format(
                    context.resources.getString(R.string.git_rejected_nondelete),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD -> {
                msg = String.format(
                    context.resources.getString(R.string.git_rejected_nonfastforward),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.REJECTED_OTHER_REASON -> {
                val reason = update.message
                msg = if (reason.isNullOrEmpty()) {
                    String.format(
                        context.resources.getString(R.string.git_rejected_other_reason),
                        update.remoteName
                    )
                } else {
                    String.format(
                        context.resources.getString(R.string.git_rejected_other_reason_detailed),
                        update.remoteName,
                        reason
                    )
                }
            }
            RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED -> {
                msg = String.format(
                    context.resources.getString(R.string.git_rejected_remote_changed),
                    update.remoteName
                )
            }
            RemoteRefUpdate.Status.UP_TO_DATE -> {
                msg = String.format(
                    context.resources.getString(R.string.git_uptodate),
                    update.remoteName
                )
            }
            else -> msg = "Unknown status"
        }
        msg += "\n" + String.format(
            context.resources.getString(R.string.git_server_details),
            remote
        )
        return msg
    }
}