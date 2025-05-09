package org.bibletranslationtools.docscanner.data.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.GitCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.errors.LockFailedException
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.StoredConfig
import java.io.File
import java.io.IOException

class Repo (val repositoryPath: String) {
    private var git: Git? = null
    private var storedConfig: StoredConfig? = null
    private var author: PersonIdent? = null
    private val remotes: MutableSet<String> = HashSet()

    init {
        // create the directory if missing
        val repoPath = File(repositoryPath)
        if (!repoPath.exists()) {
            repoPath.mkdir()
        }

        // initialize new repository
        val gitPath = File("$repositoryPath/.git")
        if (!gitPath.exists()) {
            initRepo()
        }
    }

    /**
     * Initialize the git repository
     */
    private fun initRepo() {
        val init = Git.init()
        val initFile = File(repositoryPath)
        init.setDirectory(initFile)
        try {
            init.call()
        } catch (e: GitAPIException) {
            // could not create repo
            e.printStackTrace()
        }
    }

    /**
     * Returns the repository directory
     * @return
     */
    fun getDir(): File {
        return File(repositoryPath)
    }

    @Throws(IOException::class)
    fun getGit(): Git {
        return git ?: run {
            Git.open(getDir()).also {
                git = it
            }
        }
    }

    fun getBranchName(): String? {
        return try {
            getGit().repository.getFullBranch()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getRemotes(): MutableSet<String> {
        if (remotes.isNotEmpty()) return remotes
        try {
            val config: StoredConfig = getStoredConfig()
            remotes.addAll(config.getSubsections("remote"))
            return remotes
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return hashSetOf()
    }

    @Throws(IOException::class)
    fun setRemote(remote: String, url: String) {
        try {
            val config = getStoredConfig()
            val remoteNames = config.getSubsections("remote")
            if (remoteNames.contains(remote)) {
                throw IOException(String.format("Remote %s already exists.", remote))
            }
            config.setString("remote", remote, "url", url)
            val fetch = String.format("+refs/heads/*:refs/remotes/%s/*", remote)
            config.setString("remote", remote, "fetch", fetch)
            config.save()
            remotes.add(remote)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun deleteRemote(remote: String?) {
        val config = getStoredConfig()
        config.unsetSection("remote", remote)
    }

    @Throws(IOException::class)
    fun getStoredConfig(): StoredConfig {
        return storedConfig ?: run {
            getGit().repository.config.also {
                storedConfig = it
            }
        }
    }

    /**
     * This will call a git command while attempting to handle lock exceptions.
     * If the repo is locked it will wait and try again several times before removing the lock and
     * calling the command once more. This last call may throw an exception.
     *
     * Use this with caution. You could break things by ignoring the git lock.
     *
     * @param command the command to call
     */
    @Deprecated("")
    @Throws(GitAPIException::class)
    fun forceCall(command: GitCommand<*>): Any? {
        try {
            return command.call()
        } catch (e: JGitInternalException) {
            // throw the error if not a lock exception
            val cause: Throwable? = getCause(e, LockFailedException::class.java)
            if (cause == null) throw e
        } catch (e: GitAPIException) {
            val cause: Throwable? = getCause(e, LockFailedException::class.java)
            if (cause == null) throw e
        }

        // re-try several times
        var attempts = 0
        do {
            attempts++
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            try {
                return command.call()
            } catch (e: JGitInternalException) {
                // throw the error if not a lock exception
                val cause: Throwable? = getCause(e, LockFailedException::class.java)
                if (cause == null) {
                    throw e
                }
            } catch (e: GitAPIException) {
                val cause: Throwable? = getCause(e, LockFailedException::class.java)
                if (cause == null) {
                    throw e
                }
            }
        } while (attempts < 30) // try several times up to 15 seconds

        // remove lock and call once more
        val gitDir = command.repository.directory
        val lockFile = File(gitDir, "index.lock")
        if (lockFile.exists()) lockFile.delete()
        return command.call()
    }

    @Throws(Exception::class)
    fun commit(): Boolean {
        val git: Git = getGit()

        // check if dirty
        if (isClean()) {
            return true
        }

        // stage changes
        val add = git.add()
        add.addFilepattern(".")

        try {
            add.call()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // commit changes
        val commit = git.commit()
        commit.setAll(true)
        if (author != null) {
            commit.setAuthor(author)
        }
        commit.setMessage("auto save")

        try {
            commit.call()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * Checks if the throwable has the given cause
     * @param thrown the thrown object
     * @param cause the cause class
     * @return the matched cause
     */
    private fun getCause(thrown: Throwable, cause: Class<*>): Throwable? {
        if (cause.isInstance(thrown)) return thrown
        var child = thrown.cause
        if (child == null) return null

        do {
            if (cause.isInstance(child)) return child
            child = child!!.cause
        } while (child != null)
        return null
    }

    fun setAuthor(name: String, email: String) {
        author = PersonIdent(name, email)
    }

    /**
     * Checks if there are any non-committed changes in the repo
     * @return
     * @throws Exception
     */
    fun isClean(): Boolean {
        try {
            val git: Git = getGit()
            return git.status().call().isClean
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }
}