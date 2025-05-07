package org.bibletranslationtools.docscanner.data.local.git

import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport

/**
 * Created by joel on 9/15/2014.
 */
class TransportCallback(
    directoryProvider: DirectoryProvider,
    port: Int
) : TransportConfigCallback {
    private val ssh = GitSessionFactory(directoryProvider, port)

    override fun configure(tn: Transport) {
        if (tn is SshTransport) {
            tn.sshSessionFactory = ssh
        }
    }
}
