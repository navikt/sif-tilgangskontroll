package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.spesification.evaluate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class TilgangskontrollService(
    private val tilgangsAttributter: TilgangsAttributter
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(TilgangskontrollService::class.java)
    }

    fun hentTilgangTilBarn(barnTilgangForespørsel: BarnTilgangForespørsel, bearerToken: JwtToken): PolicyEvaluation {

        val hentBarnContext = hentBarnContext(
            bearerToken,
            barnTilgangForespørsel,
            tilgangsAttributter
        )

        return evaluate(ctx = hentBarnContext,
            policy = Policies.`Barn er i live`
                .and(Policies.`NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`)

        ) {
            it
        }
    }

    fun hentTilgangTilPerson(bearerToken: JwtToken): PolicyEvaluation {
        val personContext = hentPersonContext(bearerToken, tilgangsAttributter)
        return evaluate(
            ctx = personContext,
            policy = Policies.`NAV-bruker er i live`
        ) {
            it
        }
    }
}

