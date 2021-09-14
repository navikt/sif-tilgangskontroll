package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.siftilgangskontroll.Routes.SØKNAD
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "tokenx")
class TilgangsController(
    private val søknadService: TilgangskontrollService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @GetMapping(SØKNAD, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentSøknader(): Tilgangskontroll {
        val søknader = søknadService.harTilgang()
        return søknader
    }
}
