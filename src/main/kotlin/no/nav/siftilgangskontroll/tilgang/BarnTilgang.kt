package no.nav.siftilgangskontroll.tilgang

import no.nav.siftilgangskontroll.spesification.PolicyEvaluation

data class BarnTilgangForespørsel(
    val barnIdent: List<BarnIdent>
)

data class BarnTilgangResponse(
    val barnIdent: BarnIdent,
    val policyEvaluation: PolicyEvaluation
)
