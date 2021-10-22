package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.utils.personIdent
import no.nav.siftilgangskontroll.pdl.generated.hentperson.ForelderBarnRelasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import java.time.LocalDate
import java.time.Period

const val MYNDIG_ALDER = 18

internal data class HentPersonContext(
    private val bearerToken: JwtToken,
    private val pdlService: PdlService
) {
    val pdlPerson = PdlPerson(
        borgerToken = bearerToken.tokenAsString,
        pdlService = pdlService
    )
}

data class PdlPerson(
    private val pdlService: PdlService,
    val borgerToken: String
) {
    val person = runBlocking { pdlService.person(JwtToken(borgerToken).personIdent(), borgerToken) }

    fun erDød(): Boolean = person.erDød()
    fun relasjoner(): List<ForelderBarnRelasjon> = person.forelderBarnRelasjon
    fun fødselsdato(): LocalDate = LocalDate.parse(person.foedsel.first().foedselsdato!!)
    fun erMyndig(): Boolean {
        val alder = Period.between(fødselsdato(), LocalDate.now()).years
        return when {
            alder >= MYNDIG_ALDER -> true
            else -> false
        }
    }
}

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()
fun Person.ident() = folkeregisteridentifikator.first().identifikasjonsnummer
