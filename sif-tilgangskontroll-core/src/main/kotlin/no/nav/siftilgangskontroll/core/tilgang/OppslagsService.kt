package no.nav.siftilgangskontroll.core.tilgang

import no.nav.policy.spesification.PolicyEvaluation
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.policy.spesification.evaluate
import no.nav.siftilgangskontroll.core.tilgang.Policies.`Barn er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er i live`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker er myndig`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker har tilgang relasjon`
import no.nav.siftilgangskontroll.core.tilgang.Policies.`NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`
import org.slf4j.LoggerFactory


class OppslagsService(
    private val tilgangsAttributter: TilgangsAttributter
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(OppslagsService::class.java)
    }

    fun hentBarn(barnTilgangForespørsel: BarnTilgangForespørsel, bearerToken: JwtToken, systemToken: JwtToken): List<BarnTilgangResponse> {

        val hentBarnContext = tilgangsAttributter.hentBarnContext(
            bearerToken,
            systemToken,
            barnTilgangForespørsel
        )

        return hentBarnContext.barn.barn.map { barn ->
            evaluate(
                ctx = hentBarnContext,
                policy = `Barn er i live`(barn.personIdent())
                        and `NAV-bruker har tilgang relasjon`(barn.personIdent())
                        and `NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`(barn.personIdent()),
                block = { BarnTilgangResponse(barn, it) })
        }
    }

    fun hentPerson(bearerToken: JwtToken): PersonTilgangResponse {
        val personContext = tilgangsAttributter.hentPersonContext(bearerToken)
        return evaluate(
            ctx = personContext,
            policy = `NAV-bruker er i live`() and `NAV-bruker er myndig`(),
            block = { PersonTilgangResponse(personContext.borger.person, it) })
    }
}

