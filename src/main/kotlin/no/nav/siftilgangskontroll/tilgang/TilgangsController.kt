package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.siftilgangskontroll.Routes.AKTØR_ID
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.PERSON
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.siftilgangskontroll.core.tilgang.*
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
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
    private val tilgangService: TilgangService,
    private val pdlAuthService: PdlAuthService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @GetMapping(PERSON, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilPerson(): ResponseEntity<TilgangResponsePerson> {
        val personOppslagRespons =
            tilgangService.hentPerson(pdlAuthService.borgerToken())
        logger.info("Hentet tilgang: {}", personOppslagRespons)

        return personOppslagRespons.somResponseEntity()
    }

    @PostMapping(BARN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilBarn(@RequestBody barnTilgangForespørsel: BarnTilgangForespørsel): ResponseEntity<List<TilgangResponseBarn>> {
        val barnOppslagRespons =
            tilgangService.hentBarn(barnTilgangForespørsel, pdlAuthService.borgerToken(), pdlAuthService.systemToken())
        logger.info("Hentet tilgang: {}", barnOppslagRespons)

        return barnOppslagRespons.somResponseEntity()
    }

    @PostMapping(AKTØR_ID, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilAktørId(): ResponseEntity<TilgangResponseAktørId> {
        val tilgangResponseAktørId =
            tilgangService.hentAktørId(pdlAuthService.borgerToken())
        logger.info("Hentet tilgang: {}", tilgangResponseAktørId)

        return tilgangResponseAktørId.somResponseEntity()
    }
}

private fun TilgangResponseAktørId.somResponseEntity() = when (policyEvaluation.decision) {
    PolicyDecision.PERMIT -> ResponseEntity(this, OK)
    PolicyDecision.DENY -> ResponseEntity(this, FORBIDDEN)
    PolicyDecision.NOT_APPLICABLE -> ResponseEntity(INTERNAL_SERVER_ERROR)
}

fun TilgangResponsePerson.somResponseEntity() = when (policyEvaluation.decision) {
    PolicyDecision.PERMIT -> ResponseEntity(this, OK)
    PolicyDecision.DENY -> ResponseEntity(this, FORBIDDEN)
    PolicyDecision.NOT_APPLICABLE -> ResponseEntity(INTERNAL_SERVER_ERROR)
}

fun List<TilgangResponseBarn>.somResponseEntity() = ResponseEntity(this, OK)
