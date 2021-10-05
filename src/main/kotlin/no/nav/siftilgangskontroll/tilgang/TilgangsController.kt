package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.PERSON
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.policy.spesification.PolicyDecision
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.tilgang.BarnTilgangForespørsel
import no.nav.siftilgangskontroll.core.tilgang.BarnTilgangResponse
import no.nav.siftilgangskontroll.core.tilgang.OppslagsService
import no.nav.siftilgangskontroll.core.tilgang.PersonTilgangResponse
import no.nav.siftilgangskontroll.pdl.PdlAuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(TILGANG)
@ProtectedWithClaims(issuer = "tokenx")
class TilgangsController(
    private val oppslagsService: OppslagsService,
    private val pdlAuthService: PdlAuthService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @GetMapping(PERSON, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilPerson(): ResponseEntity<PersonTilgangResponse> {
        val personOppslagRespons =
            oppslagsService.hentPerson(JwtToken(pdlAuthService.borgerToken()))
        logger.info("Hentet tilgang: {}", personOppslagRespons)

        return personOppslagRespons.somResponseEntity()
    }

    @PostMapping(BARN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilBarn(@RequestBody barnTilgangForespørsel: BarnTilgangForespørsel): ResponseEntity<List<BarnTilgangResponse>> {
        val barnOppslagRespons =
            oppslagsService.hentBarn(barnTilgangForespørsel, JwtToken(pdlAuthService.borgerToken()), JwtToken(pdlAuthService.systemToken()))
        logger.info("Hentet tilgang: {}", barnOppslagRespons)

        return barnOppslagRespons.somResponseEntity()
    }
}

fun PersonTilgangResponse.somResponseEntity() = when(policyEvaluation.decision) {
    PolicyDecision.PERMIT -> ResponseEntity(this, OK)
    PolicyDecision.DENY -> ResponseEntity(this, FORBIDDEN)
    PolicyDecision.NOT_APPLICABLE -> ResponseEntity(INTERNAL_SERVER_ERROR)
}

fun List<BarnTilgangResponse>.somResponseEntity() = ResponseEntity(this, OK)
