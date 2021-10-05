package no.nav.siftilgangskontroll.core.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.policy.spesification.Policy
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import no.nav.siftilgangskontroll.policy.spesification.equalTo
import no.nav.siftilgangskontroll.core.pdl.PdlService
import org.slf4j.Logger

typealias PersonIdent = String
typealias BarnIdent = String

data class TilgangsAttributter(
    val pdlService: PdlService
) {
    fun hentPersonContext(bearerToken: JwtToken) = HentPersonContext(bearerToken, this)

    fun hentBarnContext(bearerToken: JwtToken, systemtoken: JwtToken, barnTilgangForespørsel: BarnTilgangForespørsel) = HentBarnContext(
        barnTilgangForespørsel,
        bearerToken,
        systemtoken,
        this
    )
}

fun <T> List<T>.filterBy(policy: Policy<T>, decision: PolicyDecision, logger: Logger): List<T> = filter {
    val evaluation = policy.evaluate(it)
    when {
        evaluation equalTo decision -> true
        else -> {
            logger.debug("filtered entry, reason: ${evaluation.reason}")
            false
        }
    }
}
