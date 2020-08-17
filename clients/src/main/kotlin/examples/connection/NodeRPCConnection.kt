package examples.connection

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class NodeRPCConnection : AutoCloseable {

    @Value("\${corda.rpc.host:}")
    lateinit var host: String

    @Value("\${corda.rpc.user:}")
    lateinit var username: String

    @Value("\${corda.rpc.pass:}")
    lateinit var password: String

    @Value("\${corda.rpc.port:10002}")
    lateinit var rpcPort: String

    private lateinit var rpcConnection: CordaRPCConnection

    val proxy get() = rpcConnection.proxy

    @PostConstruct
    fun initialiseNodeRPCConnection() {
        val rpcAddress = NetworkHostAndPort(host, rpcPort.toInt())
        val rpcClient = CordaRPCClient(rpcAddress)

        rpcConnection = rpcClient.start(username, password)
    }

    @PreDestroy
    override fun close() {
        rpcConnection.notifyServerAndClose()
    }
}