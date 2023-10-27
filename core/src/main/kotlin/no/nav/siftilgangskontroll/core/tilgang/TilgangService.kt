package no.nav.siftilgangskontroll.core.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.behandling.Behandling
import no.nav.siftilgangskontroll.core.pdl.*
import no.nav.siftilgangskontroll.core.pdl.BarnContext
import no.nav.siftilgangskontroll.policy.spesification.evaluate
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er under myndighetsalder`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er myndig`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker har tilgang til barn`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er ikke adressebeskyttet`
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.HentIdenterBolkResult
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Denne klassens ansvar er å hente oppslagsdata, håndhevet gjennom et sett med policier.
 */
class TilgangService(
    private val pdlService: PdlService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(TilgangService::class.java)
    }

    /**
     * Henter barn fra PDL.
     *
     * Operasjonen håndhever et sett med policier som er avgjørende for å få tilgang til data.
     * For å få tilgang til barn må følgende policier være oppfylt:
     * - Barn er under myndighetsalder (18).
     * - NAV-bruker har tilgang til barn.
     * - Barn er ikke adressebeskyttet.
     *
     * @param barnTilgangForespørsel: Liste med identifikasjon på barn man ønsker oppslag på.
     * @param bearerToken: Sluttbrukers token. Enten Azure OBO, eller tokenX.
     * @param systemToken: Systemtoken hentet fra azure klient.
     *
     * @return TilgangResponseBarn: 'data' er null dersom det ikke gitt tilgang. Se 'policyEvaulation' for begrunnelse.
     */
    fun hentBarn(
        barnTilgangForespørsel: BarnTilgangForespørsel,
        bearerToken: String,
        systemToken: String,
        callId: String = UUID.randomUUID().toString(),
        behandling: Behandling
    ): List<TilgangResponseBarn> {

        val barnContext = BarnContext(
            barnTilgangForespørsel = barnTilgangForespørsel,
            pdlService = pdlService,
            bearerToken = JwtToken(bearerToken),
            systemtoken = JwtToken(systemToken),
            callId = callId,
            behandling = behandling
        )

        return barnContext.pdlBarn.barn.map { barn ->
            evaluate(
                ctx = barnContext,
                policy = `Barn er i live`(barn.ident())
                        and `Barn er under myndighetsalder`(barn.ident())
                        and `NAV-bruker har tilgang til barn`(barn.ident())
                        and `Barn er ikke adressebeskyttet`(
                    barn.ident()
                ),
                block = {
                    when (it.decision) {
                        PolicyDecision.PERMIT -> TilgangResponseBarn(barn.ident(), barn, it)
                        else -> TilgangResponseBarn(barn.ident(), null, it)
                    }
                })
        }
    }

    /**
     * Henter person fra PDL.
     *
     * Operasjonen håndhever et sett med policier som er avgjørende for å få tilgang til data.
     * For å få tilgang til person må følgende policier være oppfylt:
     * - NAV-bruker er i live.
     * - NAV-bruker er myndig.
     *
     * @param bearerToken: Sluttbrukers token. Enten Azure OBO, eller tokenX.
     *
     * @return TilgangResponsePerson: 'data' er null dersom det ikke gitt tilgang. Se 'policyEvaulation' for begrunnelse.
     */
    fun hentPerson(bearerToken: String, callId: String = UUID.randomUUID().toString(), behandling: Behandling): TilgangResponsePerson {
        val personContext = PdlPersonContext(
            pdlService = pdlService,
            borgerToken = bearerToken,
            callId = callId,
            behandling = behandling
        )
        return evaluate(
            ctx = personContext,
            policy = `NAV-bruker er i live`() and `NAV-bruker er myndig`(),
            block = {
                when (it.decision) {
                    PolicyDecision.PERMIT -> TilgangResponsePerson(
                        personContext.person.ident(),
                        personContext.person,
                        it
                    )
                    else -> TilgangResponsePerson(personContext.person.ident(), null, it)
                }
            })
    }

    fun hentAktørId(ident: String, identGruppe: IdentGruppe, borgerToken: String, callId: String = UUID.randomUUID().toString()): AktørId {
        return PdlAktørIdContext(
            pdlService = pdlService,
            ident = ident,
            identGruppe = identGruppe,
            borgerToken = borgerToken,
            callId = callId
        ).identer.tilAktørId()
    }

    fun hentIdenter(identer: List<String>, identGrupper: List<IdentGruppe>, systemToken: String, callId: String = UUID.randomUUID().toString()): List<HentIdenterBolkResult> {
        return PdlIdenterBolkContext(
            pdlService = pdlService,
            identer = identer,
            identGrupper = identGrupper,
            systemToken = systemToken,
            callId = callId
        ).identerBolkResults
    }
}

