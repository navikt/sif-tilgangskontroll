package no.nav.siftilgangskontroll.core.tilgang

import no.nav.policy.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person as BarnPerson

data class BarnTilgangForespørsel(
    val barnIdenter: List<BarnIdent>
)

data class BarnTilgangResponse(
    val data: BarnPerson? = null,
    val policyEvaluation: PolicyEvaluation
)

data class PersonTilgangResponse(
    val data: Person? = null,
    val policyEvaluation: PolicyEvaluation
)
