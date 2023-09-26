package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.siftilgangskontroll.Routes.AKTØR_ID
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.IDENTER
import no.nav.siftilgangskontroll.Routes.PERSON
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.siftilgangskontroll.core.pdl.AktørId
import no.nav.siftilgangskontroll.core.tilgang.*
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import no.nav.siftilgangskontroll.pdl.PdlAuthService
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.HentIdenterBolkResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit
import org.springframework.web.bind.annotation.*
import java.lang.Byte
import java.util.*

@RestController
@RequestMapping(TILGANG)
@ProtectedWithClaims(issuer = "tokenx")
class TilgangsController(
    private val tilgangService: TilgangService, private val pdlAuthService: PdlAuthService
) {
    companion object {
        val logger = LoggerFactory.getLogger(TilgangsController::class.java)
    }

    @GetMapping(PERSON, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilPerson(): ResponseEntity<TilgangResponsePerson> {
        val personOppslagRespons = tilgangService.hentPerson(
            bearerToken = pdlAuthService.borgerToken(), callId = "sif-tilgangskontroll-${UUID.randomUUID()}"
        )
        logger.info("Hentet tilgang: {}", personOppslagRespons)

        return personOppslagRespons.somResponseEntity()
    }

    @PostMapping(BARN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilBarn(@RequestBody barnTilgangForespørsel: BarnTilgangForespørsel): ResponseEntity<List<TilgangResponseBarn>> {
        val barnOppslagRespons = tilgangService.hentBarn(
            barnTilgangForespørsel = barnTilgangForespørsel,
            bearerToken = pdlAuthService.borgerToken(),
            systemToken = pdlAuthService.systemToken(),
            callId = "sif-tilgangskontroll-${UUID.randomUUID()}"
        )
        logger.info("Hentet tilgang: {}", barnOppslagRespons)

        return barnOppslagRespons.somResponseEntity()
    }

    @PostMapping(AKTØR_ID, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentTilgangTilAktørId(@RequestBody aktørIdTilgangForespørsel: AktørIdTilgangForespørsel): AktørId {
        val aktørId = tilgangService.hentAktørId(
            ident = aktørIdTilgangForespørsel.ident,
            identGruppe = IdentGruppe.AKTORID,
            borgerToken = pdlAuthService.borgerToken(),
            callId = "sif-tilgangskontroll-${UUID.randomUUID()}"
        )
        logger.info("Hentet aktørId: {}", aktørId)

        return aktørId
    }

    @PostMapping(IDENTER, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Protected
    @ResponseStatus(OK)
    fun hentIdenter(@RequestBody hentIdenterForespørsel: HentIdenterForespørsel): List<HentIdenterBolkResult> {
        val identerBolkResults = tilgangService.hentIdenter(
            identer = hentIdenterForespørsel.identer,
            identGrupper = hentIdenterForespørsel.identGrupper,
            systemToken = pdlAuthService.systemToken(),
            callId = "sif-tilgangskontroll-${UUID.randomUUID()}"
        )
        logger.info("Hentet aktørId: {}", identerBolkResults)

        return identerBolkResults
    }
}


fun TilgangResponsePerson.somResponseEntity() = when (policyEvaluation.decision) {
    PolicyDecision.PERMIT -> ResponseEntity(this, OK)
    PolicyDecision.DENY -> ResponseEntity(this, FORBIDDEN)
    PolicyDecision.NOT_APPLICABLE -> ResponseEntity(INTERNAL_SERVER_ERROR)
}

fun List<TilgangResponseBarn>.somResponseEntity() = ResponseEntity(this, OK)
