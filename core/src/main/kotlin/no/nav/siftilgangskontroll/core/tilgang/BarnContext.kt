package no.nav.siftilgangskontroll.core.tilgang

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person

data class Barn(
    val barnIdent: List<ID>,
    val systemToken: String,
    val tilgangsAttributter: TilgangsAttributter
) {
    val barn = runBlocking { tilgangsAttributter.pdlService.barn(barnIdent, systemToken) }

    fun harStrengtFortroligAdresse(ident: BarnIdent): Boolean = barn
        .filtererPåIdent(ident)
        .harStrengtFortroligAdresse()

    fun erDød(ident: BarnIdent) = barn
        .filtererPåIdent(ident)
        .erDød()
}

data class HentBarnContext(
    val barnTilgangForespørsel: BarnTilgangForespørsel,
    private val bearerToken: JwtToken,
    private val systemtoken: JwtToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        borgerToken = bearerToken.tokenAsString,
        tilgangsAttributter = tilgangsAttributter
    )

    val barn = Barn(
        barnIdent = barnTilgangForespørsel.barnIdenter,
        systemToken = systemtoken.tokenAsString,
        tilgangsAttributter = tilgangsAttributter
    )
}

fun Person.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()

fun List<Person>.filtererPåIdent(ident: BarnIdent) =
    first { it.folkeregisteridentifikator.first().identifikasjonsnummer == ident }

fun Person.ident() = folkeregisteridentifikator.first().identifikasjonsnummer
