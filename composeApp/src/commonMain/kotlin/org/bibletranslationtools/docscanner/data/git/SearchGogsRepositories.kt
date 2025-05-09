package org.bibletranslationtools.docscanner.data.git

import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.gogs_user_agent
import docscanner.composeapp.generated.resources.pref_default_gogs_api
import org.bibletranslationtools.docscanner.OnProgressListener
import org.bibletranslationtools.docscanner.data.Settings
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.jetbrains.compose.resources.getString
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Repository

class SearchGogsRepositories(
    private val prefRepo: PreferenceRepository,
) {
    private val max = 100

    suspend fun execute(
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
                getString(Res.string.pref_default_gogs_api)
            ),
            getString(Res.string.gogs_user_agent)
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