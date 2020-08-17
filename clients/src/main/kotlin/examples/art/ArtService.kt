package examples.art

import examples.ArtCreateFlowInitiator
import examples.ArtState
import examples.connection.NodeRPCConnection
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ArtService {

    private val log = LoggerFactory.getLogger(ArtService::class.java)!!

    @Autowired
    lateinit var cordaRpc: NodeRPCConnection

    private val proxy get() = cordaRpc.proxy

    fun createArt(art: Art): Art {
        val createArtFlow = proxy.startTrackedFlow(::ArtCreateFlowInitiator, art.title, art.artist)
        createArtFlow.progress.subscribe(log::info)

        val signTx = createArtFlow.returnValue.getOrThrow()

        val artState = signTx.tx.outputsOfType<ArtState>().single()

        return Art(artState.title, artState.artist, artState.appraiser.toString(), artState.owner.toString())
    }

    fun findArts(): List<Art> {
        val result = proxy.vaultQueryBy<ArtState>()
        return result.states.map {
            Art(it.state.data.title, it.state.data.artist, it.state.data.appraiser.toString(), it.state.data.owner.toString())
        }
    }

}