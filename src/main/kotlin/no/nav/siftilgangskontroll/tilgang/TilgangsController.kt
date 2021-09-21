package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.util.bearerToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(TILGANG)
@ProtectedWithClaims(issuer = "tokenx")
class TilgangsController(
    private val contextHolder: TokenValidationContextHolder,
    private val tilgangskontrollService: TilgangskontrollService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @PostMapping(BARN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilBarn(@RequestBody barnTilgangForespørsel: BarnTilgangForespørsel): PolicyEvaluation? {
        val bearerToken = contextHolder.bearerToken()
        val tilgangskontroll =
            tilgangskontrollService.hentTilgangTilBarn(barnTilgangForespørsel, bearerToken.tokenAsString)
        logger.info("Hentet tilgang: {}", tilgangskontroll)
        return tilgangskontroll
    }
}
