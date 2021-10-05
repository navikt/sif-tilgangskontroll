package no.nav.siftilgangskontroll.core.tilgang

import no.nav.policy.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person

data class BarnTilgangForespørsel(
    val barnIdenter: List<BarnIdent>
)

data class BarnTilgangResponse(
    val barnIdent: Person? = null,
    val policyEvaluation: PolicyEvaluation
)
