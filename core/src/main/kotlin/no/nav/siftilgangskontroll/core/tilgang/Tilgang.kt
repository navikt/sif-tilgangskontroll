package no.nav.siftilgangskontroll.core.tilgang

import no.nav.siftilgangskontroll.core.pdl.BarnIdent
import no.nav.siftilgangskontroll.core.pdl.AktørId
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.policy.spesification.PolicyEvaluation

data class BarnTilgangForespørsel(
    val barnIdenter: List<BarnIdent>
)

data class TilgangResponsePerson(
    val ident: String,
    val person: Person? = null,
    val policyEvaluation: PolicyEvaluation
)

data class TilgangResponseBarn(
    val ident: String,
    val barn: no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person? = null,
    val policyEvaluation: PolicyEvaluation
)

data class TilgangResponseAktørId(
    val ident: String,
    val aktørId: AktørId? = null,
    val policyEvaluation: PolicyEvaluation
)
