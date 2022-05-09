package no.nav.siftilgangskontroll.wiremock

import no.nav.siftilgangskontroll.pdl.generated.enums.ForelderBarnRelasjonRolle
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.*
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse as BarnAdressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Doedsfall as BarnDoedsfall
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Foedsel as BarnFoedsel
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Folkeregisteridentifikator as BarnFolkeregisteridentifikator
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Navn as BarnNavn
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person as Barn

object PdlResponses {
    fun defaultHentPersonResult(
        fødselsdato: Foedsel = Foedsel("1990-09-27"),
        navn: Navn = Navn(
            fornavn = "Ole",
            mellomnavn = null,
            etternavn = "Doffen",
            forkortetNavn = "Ole Doffen"
        ),
        dødsdato: Doedsfall? = null,
        adressebeskyttelse: Adressebeskyttelse? = null,
        relatertPersonsIdent: String? = "123"
    ) = Person(
        folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
        foedsel = listOf(fødselsdato),
        navn = listOf(navn),
        doedsfall = dødsdato?.let { listOf(it) } ?: listOf(),
        adressebeskyttelse = adressebeskyttelse?.let { listOf(it) } ?: listOf(),
        forelderBarnRelasjon = listOf(
            ForelderBarnRelasjon(
                relatertPersonsIdent = relatertPersonsIdent,
                relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                minRolleForPerson = null
            )
        )
    )

    fun defaultHentPersonBolkResult(
        folkeregisteridentifikator: BarnFolkeregisteridentifikator = BarnFolkeregisteridentifikator("123"),
        fødselsdato: BarnFoedsel = BarnFoedsel("2020-01-01"),
        navn: BarnNavn = BarnNavn(
            fornavn = "Dole",
            mellomnavn = null,
            etternavn = "Doffen",
            forkortetNavn = "Dole Doffen"
        ),
        dødsdato: BarnDoedsfall? = null,
        adressebeskyttelse: BarnAdressebeskyttelse? = null
    ) = HentPersonBolkResult(
        code = "200",
        person = Barn(
            folkeregisteridentifikator = listOf(folkeregisteridentifikator),
            foedsel = listOf(fødselsdato),
            navn = listOf(navn),
            doedsfall = dødsdato?.let { listOf(it) } ?: listOf(),
            adressebeskyttelse = adressebeskyttelse?.let { listOf(it) } ?: listOf()
        )
    )

    fun defaultHentIdenterResult(ident: String, identGruppe: IdentGruppe) = listOf(
        IdentInformasjon(ident = ident, historisk = false, gruppe = identGruppe)
    )
}
