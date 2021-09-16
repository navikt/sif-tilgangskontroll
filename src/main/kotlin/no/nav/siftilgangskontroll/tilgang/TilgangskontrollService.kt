package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.spesification.authorize
import no.nav.siftilgangskontroll.util.bearerToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class TilgangskontrollService(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val tokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val tilgangsAttributter: TilgangsAttributter
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(TilgangskontrollService::class.java)
    }

    fun hentTilgangTilBarn(barnTilgangForespørsel: BarnTilgangForespørsel): PolicyEvaluation? {

        val hentBarnContext = hentBarnContext(
            tokenValidationContextHolder.bearerToken().tokenAsString,
            barnTilgangForespørsel,
            tilgangsAttributter
        )

        return authorize(hentBarnContext, Policies.`borgers tilgang til barn med strengt fortrolig adresse`) {
            logger.debug("access is approved.")
            it
        }
    }
}

