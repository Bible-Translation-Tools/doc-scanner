package org.bibletranslationtools.docscanner.data.git

import org.bibletranslationtools.docscanner.OnProgressListener
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.unfoldingword.gogsclient.Repository
import org.unfoldingword.gogsclient.User

class GetRepository(
    private val createRepository: CreateRepository,
    private val searchRepository: SearchGogsRepositories
) {
    private val max = 100

    suspend fun execute(
        project: Project,
        user: User,
        progressListener: OnProgressListener? = null
    ): Repository? {
        progressListener?.onProgress(-1, max, "Getting repository")

        // Create repository
        // If it exists, will do nothing
        createRepository.execute(project, user, progressListener)

        // Search for repository
        // There could be more than one repo, which name can contain requested repo name.
        // For example: en_ulb_mat_txt, custom_en_ulb_mat_text, en_ulb_mat_text_l3, etc.
        // Setting limit to 100 should be enough to cover most of the cases.
        val repositories = searchRepository.execute(
            user.id,
            project.getName(),
            100,
            progressListener
        )

        if (repositories.isNotEmpty()) {
            for (repo in repositories) {
                // Filter repos to have exact user and repo name
                if (repo.owner.username == user.username &&
                    repo.name == project.getName()) {
                    return repo
                }
            }
        }

        return null
    }
}