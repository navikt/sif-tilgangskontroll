package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.pdl.PdlService
import no.nav.policy.spesification.Policy
import no.nav.policy.spesification.PolicyDecision
import no.nav.policy.spesification.equalTo
import org.slf4j.Logger
import org.springframework.stereotype.Component

typealias PersonIdent = String
typealias BarnIdent = String

@Component
data class TilgangsAttributter(
    val pdlService: PdlService
) {
    fun hentPersonContext(bearerToken: JwtToken) = HentPersonContext(bearerToken, this)

    fun hentBarnContext(bearerToken: JwtToken, barnTilgangForespørsel: BarnTilgangForespørsel) = HentBarnContext(
        barnTilgangForespørsel,
        bearerToken,
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
