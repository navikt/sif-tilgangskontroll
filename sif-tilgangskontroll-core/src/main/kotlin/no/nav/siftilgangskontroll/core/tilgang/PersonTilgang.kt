package no.nav.siftilgangskontroll.core.tilgang

import no.nav.policy.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person

data class PersonTilgangForespørsel(
    val personIdent: PersonIdent
)

data class PersonTilgangResponse(
    val personIdent: Person? = null,
    val policyEvaluation: PolicyEvaluation
)
