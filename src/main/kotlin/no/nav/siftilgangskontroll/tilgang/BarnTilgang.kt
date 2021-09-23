package no.nav.siftilgangskontroll.tilgang

import no.nav.siftilgangskontroll.spesification.PolicyEvaluation

data class BarnTilgangForespørsel(
    val barnIdenter: List<BarnIdent>
)

data class BarnTilgangResponse(
    val barnIdent: BarnIdent,
    val policyEvaluation: PolicyEvaluation
)
