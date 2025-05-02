package org.bibletranslationtools.docscanner.data.local.git

import android.content.Context
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Repository

class SearchGogsRepositories(
    private val context: Context,
    private val prefRepo: PreferenceRepository,
) {
    private val max = 100

    fun execute(
        uid: Int,
        query: String,
        limit: Int,
        progressListener: OnProgressListener? = null
    ): List<Repository> {
        progressListener?.onProgress(-1, max, "Searching for repositories")
        val repositories = arrayListOf<Repository>()

        val repoQuery = query.ifEmpty { "_" }

        val api = GogsAPI(
            prefRepo.getPref(
                Settings.KEY_PREF_GOGS_API,
                context.getString(R.string.pref_default_gogs_api)
            ),
            context.getString(R.string.gogs_user_agent)
        )
        val repos = api.searchRepos(repoQuery, uid, limit)

        // fetch additional information about the repos (clone urls)
        for (repo in repos) {
            val extraRepo = api.getRepo(repo, null)
            if (extraRepo != null) {
                repositories.add(extraRepo)
            }
        }

        return repositories
    }
}