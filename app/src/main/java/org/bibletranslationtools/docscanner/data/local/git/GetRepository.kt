package org.bibletranslationtools.docscanner.data.local.git

import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.models.ProjectWithData
import org.bibletranslationtools.docscanner.data.models.getName
import org.unfoldingword.gogsclient.Repository
import kotlin.collections.isNotEmpty

class GetRepository(
    private val createRepository: CreateRepository,
    private val searchRepository: SearchGogsRepositories
) {
    private val max = 100

    fun execute(
        project: ProjectWithData,
        profile: Profile,
        progressListener: OnProgressListener? = null
    ): Repository? {
        progressListener?.onProgress(-1, max, "Getting repository")

        if (profile.gogsUser == null) {
            println("Gogs user is not set")
            return null
        }

        // Create repository
        // If it exists, will do nothing
        createRepository.execute(project, profile, progressListener)

        // Search for repository
        // There could be more than one repo, which name can contain requested repo name.
        // For example: en_ulb_mat_txt, custom_en_ulb_mat_text, en_ulb_mat_text_l3, etc.
        // Setting limit to 100 should be enough to cover most of the cases.
        val repositories = searchRepository.execute(
            profile.gogsUser!!.id,
            project.getName(),
            100,
            progressListener
        )

        if (repositories.isNotEmpty()) {
            for (repo in repositories) {
                // Filter repos to have exact user and repo name
                if (repo.owner.username == profile.gogsUser!!.username &&
                    repo.name == project.getName()) {
                    return repo
                }
            }
        }

        return null
    }
}