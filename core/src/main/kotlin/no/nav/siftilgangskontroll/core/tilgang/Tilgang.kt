package no.nav.siftilgangskontroll.core.tilgang

import no.nav.siftilgangskontroll.policy.spesification.PolicyEvaluation

data class BarnTilgangForespørsel(
    val barnIdenter: List<BarnIdent>
)

data class TilgangResponse(
    val ident: String,
    val policyEvaluation: PolicyEvaluation
)
