package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.behandling.Behandling
import no.nav.siftilgangskontroll.core.utils.personIdent
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import java.time.LocalDate

data class PdlRelatertPersonOppslagContext(
    private val pdlService: PdlService,
    private val behandling: Behandling,
    val ident: String,
    val borgerToken: String,
    val callId: String,
) {
    val person: Person = runBlocking { pdlService.person(ident, borgerToken, callId, behandling) }
    fun fødselsdato(): LocalDate = LocalDate.parse(person.foedsel.first().foedselsdato!!)
    fun erKjentRelasjon(): Boolean = person.forelderBarnRelasjon
        .filterNot { it.relatertPersonsIdent == null }
        .any { it.relatertPersonsIdent == JwtToken(borgerToken).personIdent() }
}
