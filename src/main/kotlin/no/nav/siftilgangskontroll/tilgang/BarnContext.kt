package no.nav.siftilgangskontroll.tilgang

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person
import no.nav.siftilgangskontroll.util.personIdent

data class Barn(
    val barnIdent: List<ID>,
    val tilgangsAttributter: TilgangsAttributter
) {
    val barn = runBlocking { tilgangsAttributter.pdlService.barn(barnIdent) }

    fun harStrengtFortroligAdresse(ident: BarnIdent): Boolean = barn
        .filtererPåIdent(ident)
        .person!!.harStrengtFortroligAdresse()

    fun erDød(ident: BarnIdent) = barn
        .filtererPåIdent(ident)
        .person!!.erDød()
}

data class HentBarnContext(
    val barnTilgangForespørsel: BarnTilgangForespørsel,
    private val bearerToken: JwtToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        personIdent = bearerToken.personIdent(),
        tilgangsAttributter = tilgangsAttributter
    )

    val barn = Barn(
        barnIdent = barnTilgangForespørsel.barnIdenter,
        tilgangsAttributter = tilgangsAttributter
    )
}

fun Person.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()

fun List<HentPersonBolkResult>.filtererPåIdent(ident: BarnIdent) =
    first { it.person!!.folkeregisteridentifikator.first().identifikasjonsnummer == ident }

fun HentPersonBolkResult.personIdent() = this.person!!.folkeregisteridentifikator.first().identifikasjonsnummer
