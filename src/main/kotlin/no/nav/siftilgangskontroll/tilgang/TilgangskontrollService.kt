package no.nav.siftilgangskontroll.tilgang

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

    fun hentTilgangTilBarn(barnTilgangForespørsel: BarnTilgangForespørsel, bearerToken: String): PolicyEvaluation? {

        val hentBarnContext = hentBarnContext(
            bearerToken,
            barnTilgangForespørsel,
            tilgangsAttributter
        )

        return evaluate(hentBarnContext, Policies.`borgers tilgang til barn med strengt fortrolig adresse`) {
            logger.debug("access is approved.")
            it
        }
    }
}

