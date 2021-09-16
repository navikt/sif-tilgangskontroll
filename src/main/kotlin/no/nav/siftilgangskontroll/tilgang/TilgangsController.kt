package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(TILGANG)
@ProtectedWithClaims(issuer = "tokenx")
class TilgangsController(
    private val tilgangskontrollService: TilgangskontrollService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @PostMapping(BARN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentSøknader(@RequestBody barnTilgangForespørsel: BarnTilgangForespørsel): PolicyEvaluation? {
        val tilgangskontroll = tilgangskontrollService.hentTilgangTilBarn(barnTilgangForespørsel)
        logger.info("Hentet tilgang: {}", tilgangskontroll)
        return tilgangskontroll
    }
}
