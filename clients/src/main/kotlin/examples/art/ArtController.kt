package examples.art

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/art", produces = ["application/json"])
class ArtController {

    @Autowired
    lateinit var artService: ArtService

    @PostMapping()
    fun createArt(
            @RequestBody request: Art
    ): Art {
        return artService.createArt(request)
    }

    @GetMapping()
    fun findArts(): List<Art> {
        return artService.findArts()
    }
}