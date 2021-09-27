package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.spesification.evaluate
import no.nav.siftilgangskontroll.tilgang.Policies.`Barn er i live`
import no.nav.siftilgangskontroll.tilgang.Policies.`NAV-bruker er i live`
import no.nav.siftilgangskontroll.tilgang.Policies.`NAV-bruker skal ikke ha tilgang til ukjent relasjon`
import no.nav.siftilgangskontroll.tilgang.Policies.`NAV-bruker under myndighetsalder`
import no.nav.siftilgangskontroll.tilgang.Policies.`NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class TilgangskontrollService(
    private val tilgangsAttributter: TilgangsAttributter
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(TilgangskontrollService::class.java)
    }

    fun hentTilgangTilBarn(barnTilgangForespørsel: BarnTilgangForespørsel, bearerToken: JwtToken): List<BarnTilgangResponse> {

        val hentBarnContext = tilgangsAttributter.hentBarnContext(
            bearerToken,
            barnTilgangForespørsel
        )

        return hentBarnContext.barn.barn.map { barn ->
            evaluate(
                ctx = hentBarnContext,
                policy = `Barn er i live`(barn.personIdent())
                        and `NAV-bruker skal ikke ha tilgang til ukjent relasjon`(barn.personIdent())
                        and `NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`(barn.personIdent()),
                block = { BarnTilgangResponse(barn.personIdent(), it) })
        }
    }

    fun hentTilgangTilPerson(bearerToken: JwtToken): PolicyEvaluation {
        val personContext = tilgangsAttributter.hentPersonContext(bearerToken)
        return evaluate(
            ctx = personContext,
            policy = `NAV-bruker er i live`() and `NAV-bruker under myndighetsalder`(),
            block = { it })
    }
}

