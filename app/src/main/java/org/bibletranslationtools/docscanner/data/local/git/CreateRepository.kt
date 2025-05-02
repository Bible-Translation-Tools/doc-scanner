package org.bibletranslationtools.docscanner.data.local.git

import android.content.Context
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Repository

class CreateRepository(
    private val context: Context,
    private val prefRepo: PreferenceRepository
) {
    private val max = 100

    fun execute(
        project: Project,
        profile: Profile,
        progressListener: OnProgressListener? = null
    ): Boolean {
        progressListener?.onProgress(-1, max, "Preparing location on server")

        val api = GogsAPI(
            prefRepo.getPref(
                Settings.KEY_PREF_GOGS_API,
                context.getString(R.string.pref_default_gogs_api)
            ),
            context.getString(R.string.gogs_user_agent)
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