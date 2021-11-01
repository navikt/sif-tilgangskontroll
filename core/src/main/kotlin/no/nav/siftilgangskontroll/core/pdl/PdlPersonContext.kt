package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.utils.personIdent
import no.nav.siftilgangskontroll.pdl.generated.hentperson.ForelderBarnRelasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import java.time.LocalDate
import java.time.Period

const val MYNDIG_ALDER = 18

data class PdlPersonContext(
    private val pdlService: PdlService,
    val borgerToken: String,
    val callId: String
) {
    val person = runBlocking { pdlService.person(JwtToken(borgerToken).personIdent(), borgerToken, callId) }

    fun erDød(): Boolean = person.erDød()
    fun relasjoner(): List<ForelderBarnRelasjon> = person.forelderBarnRelasjon
    fun fødselsdato(): LocalDate = LocalDate.parse(person.foedsel.first().foedselsdato!!)
    fun erMyndig(): Boolean {
        val alder = Period.between(fødselsdato(), LocalDate.now()).years

        return alder >= MYNDIG_ALDER
    }
}

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()
fun Person.ident() = folkeregisteridentifikator.first().identifikasjonsnummer
