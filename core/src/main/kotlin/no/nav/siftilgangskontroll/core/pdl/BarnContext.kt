package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.tilgang.BarnTilgangForespørsel
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person
import java.time.LocalDate
import java.time.Period

typealias BarnIdent = String

data class PdlBarn(
    private val pdlService: PdlService,
    val barnIdent: List<ID>,
    val systemToken: String
) {
    val barn = runBlocking { pdlService.barn(barnIdent, systemToken) }

    fun harAdresseSkjerming(ident: BarnIdent): Boolean = barn
        .filtererPåIdent(ident)
        .harAdresseSkjerming()

    fun erDød(ident: BarnIdent) = barn
        .filtererPåIdent(ident)
        .erDød()

    fun fødselsdato(ident: BarnIdent): LocalDate = barn
        .filtererPåIdent(ident)
        .fødselsdato()

    fun erMyndig(ident: BarnIdent): Boolean {
        val alder = Period.between(fødselsdato(ident), LocalDate.now()).years
        return alder >= MYNDIG_ALDER
    }
}

internal data class BarnContext(
    val barnTilgangForespørsel: BarnTilgangForespørsel,
    val pdlService: PdlService,
    private val bearerToken: JwtToken,
    private val systemtoken: JwtToken
) {
    val pdlPersonContext = PdlPersonContext(
        borgerToken = bearerToken.tokenAsString,
        pdlService = pdlService

    )

    val pdlBarn = PdlBarn(
        pdlService = pdlService,
        barnIdent = barnTilgangForespørsel.barnIdenter,
        systemToken = systemtoken.tokenAsString
    )
}

/**
 * Feltet adressebeskyttelse er ugradert når den er tom.
 * Ellers vil den kunne inneholde en eller flere av disse verdiene: FORTROLIG STRENGT_FORTROLIG STRENGT_FORTROLIG_UTLAND.
 */
fun Person.harAdresseSkjerming(): Boolean = adressebeskyttelse.isNotEmpty()

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()
fun Person.fødselsdato(): LocalDate = LocalDate.parse(foedsel.first().foedselsdato!!)

fun List<Person>.filtererPåIdent(ident: BarnIdent) =
    first { it.folkeregisteridentifikator.first().identifikasjonsnummer == ident }

fun Person.ident() = folkeregisteridentifikator.first().identifikasjonsnummer
