package org.bibletranslationtools.docscanner.data.local.git

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.util.FS

/**
 * Created by joel on 9/15/2014.
 */
class GitSessionFactory(
    private val directoryProvider: DirectoryProvider,
    private val port: Int
) : JschConfigSessionFactory() {
    override fun configure(host: OpenSshConfig.Host, session: Session) {
        session.setConfig("StrictHostKeyChecking", "no")
        session.setConfig("PreferredAuthentications", "publickey,password")
        session.port = port
    }

    override fun createDefaultJSch(fs: FS): JSch {
        val jsch = JSch()
        val privateKey = directoryProvider.privateKey
        val publicKey = directoryProvider.publicKey
        if (directoryProvider.hasSSHKeys()) {
            jsch.addIdentity(privateKey.toString())
            SystemFileSystem.source(publicKey).buffered().use { source ->
                jsch.setKnownHosts(source.readString())
            }
        }
        return jsch
    }
}
