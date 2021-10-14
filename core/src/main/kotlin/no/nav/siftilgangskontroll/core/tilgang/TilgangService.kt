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
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker har tilgang relasjon`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import org.slf4j.LoggerFactory


class TilgangService(
    private val pdlService: PdlService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(TilgangService::class.java)
    }

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
                        and `NAV-bruker har tilgang relasjon`(barn.ident())
                        and `NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`(
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

