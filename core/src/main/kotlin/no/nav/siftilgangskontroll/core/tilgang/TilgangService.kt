package no.nav.siftilgangskontroll.core.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.pdl.BarnContext
import no.nav.siftilgangskontroll.core.pdl.HentPersonContext
import no.nav.siftilgangskontroll.core.pdl.PdlService
import no.nav.siftilgangskontroll.core.pdl.ident
import no.nav.siftilgangskontroll.policy.spesification.evaluate
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er under myndighetsalder`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er myndig`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker har tilgang barn`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er ikke adressebeskyttet`
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import org.slf4j.LoggerFactory

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
        systemToken: String
    ): List<TilgangResponseBarn> {

        val barnContext = BarnContext(
            barnTilgangForespørsel = barnTilgangForespørsel,
            pdlService = pdlService,
            bearerToken = JwtToken(bearerToken),
            systemtoken = JwtToken(systemToken)
        )

        return barnContext.pdlBarn.barn.map { barn ->
            evaluate(
                ctx = barnContext,
                policy = `Barn er i live`(barn.ident())
                        and `Barn er under myndighetsalder`(barn.ident())
                        and `NAV-bruker har tilgang barn`(barn.ident())
                        and `Barn er ikke adressebeskyttet`(
                    barn.ident()
                ),
                block = {
                    when(it.decision) {
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
    fun hentPerson(bearerToken: String): TilgangResponsePerson {
        val personContext = HentPersonContext(bearerToken =JwtToken(bearerToken), pdlService = pdlService)
        return evaluate(
            ctx = personContext,
            policy = `NAV-bruker er i live`() and `NAV-bruker er myndig`(),
            block = {
                when(it.decision) {
                    PolicyDecision.PERMIT -> TilgangResponsePerson(personContext.pdlPerson.person.ident(), personContext.pdlPerson.person, it)
                    else -> TilgangResponsePerson(personContext.pdlPerson.person.ident(), null, it)
                }
            })
    }
}

