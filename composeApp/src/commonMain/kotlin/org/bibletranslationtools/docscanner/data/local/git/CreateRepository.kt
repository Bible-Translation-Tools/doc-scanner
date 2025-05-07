package org.bibletranslationtools.docscanner.data.local.git

import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.gogs_user_agent
import docscanner.composeapp.generated.resources.pref_default_gogs_api
import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.jetbrains.compose.resources.getString
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Repository

class CreateRepository(
    private val prefRepo: PreferenceRepository
) {
    private val max = 100

    suspend fun execute(
        project: Project,
        profile: Profile,
        progressListener: OnProgressListener? = null
    ): Boolean {
        progressListener?.onProgress(-1, max, "Preparing location on server")

        val api = GogsAPI(
            prefRepo.getPref(
                Settings.KEY_PREF_GOGS_API,
                getString(Res.string.pref_default_gogs_api)
            ),
            getString(Res.string.gogs_user_agent)
        )
        if (profile.gogsUser != null) {
            val templateRepo = Repository(project.getName(), "", false)
            val repo = api.createRepo(templateRepo, profile.gogsUser)
            if (repo != null) {
                return true
            } else {
                val response = api.lastResponse
                if (response.code == 409) {
                    // Repository already exists
                    return true
                }
                println("Failed to create repository " + project.getName() + ". Gogs responded with " + response.code + ": " + response.data)
                response.exception?.printStackTrace()
            }
        }

        return false
    }
}