package no.nav.siftilgangskontroll.tilgang

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentperson.ForelderBarnRelasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.util.personIdent

data class HentPersonContext(
    private val bearerToken: JwtToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        personIdent = bearerToken.personIdent(),
        tilgangsAttributter = tilgangsAttributter
    )
}

data class Borger(
    val personIdent: PersonIdent,
    val tilgangsAttributter: TilgangsAttributter
) {
    val person = runBlocking { tilgangsAttributter.pdlService.person(personIdent) }

    fun harStrengtFortroligAdresse(): Boolean = person.harStrengtFortroligAdresse()
    fun erDød(): Boolean = person.erDød()
    fun relasjoner(): List<ForelderBarnRelasjon> = person.forelderBarnRelasjon
}

fun Person.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()
