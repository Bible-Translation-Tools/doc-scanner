package org.bibletranslationtools.docscanner.data.git

import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.gogs_user_agent
import docscanner.composeapp.generated.resources.pref_default_gogs_api
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bibletranslationtools.docscanner.OnProgressListener
import org.bibletranslationtools.docscanner.data.Settings
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.utils.trimMultiline
import org.jetbrains.compose.resources.getString
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Repository
import org.unfoldingword.gogsclient.User

class CreateRepository(
    private val prefRepo: PreferenceRepository
) {
    private val max = 100
    private val logger = KotlinLogging.logger {}

    suspend fun execute(
        project: Project,
        user: User,
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

        val templateRepo = Repository(project.getName(), "", false)
        val repo = api.createRepo(templateRepo, user)

        if (repo != null) {
            return true
        } else {
            val response = api.lastResponse
            if (response.code == 409) {
                logger.info { "Repository ${project.getName()} already exists" }
                return true
            }
            logger.warn {
                """
                    Failed to create repository ${project.getName()}.
                    Gogs responded with ${response.code}: ${response.data}
                """.trimMultiline()
            }
            response.exception?.printStackTrace()
        }

        return false
    }
}