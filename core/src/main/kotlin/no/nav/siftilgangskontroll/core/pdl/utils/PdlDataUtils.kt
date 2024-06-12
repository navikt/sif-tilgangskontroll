package no.nav.siftilgangskontroll.core.pdl.utils

import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.HentIdenterBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentperson.*
import org.json.JSONArray
import org.json.JSONObject

fun Person.somJsonObject() = JSONObject(this)
fun List<HentPersonBolkResult>.somJsonArray() = JSONArray(this)
@JvmName("somJsonArrayHentIdenterBolkResult")
fun List<HentIdenterBolkResult>.somJsonArray() = JSONArray(this)

fun List<Navn>.navnSomJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "fornavn" to it.fornavn,
            "mellomnavn" to it.mellomnavn,
            "etternavn" to it.etternavn
        ))
    }
)

fun List<IdentInformasjon>.identerSomJsonArray() = JSONArray(this)

fun List<AdressebeskyttelseGradering>.adressebeskyttelseGraderingJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "gradering" to it.name
        ))
    }
)

fun List<Folkeregisteridentifikator>.folkeregisteridentifikatorSomJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "identifikasjonsnummer" to it.identifikasjonsnummer
        ))
    }
)

fun List<ForelderBarnRelasjon>.forelderBarnRelasjonSomJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "relatertPersonsIdent" to it.relatertPersonsIdent,
            "relatertPersonsRolle" to it.relatertPersonsRolle,
            "minRolleForPerson" to it.minRolleForPerson,
        ))
    }
)

fun List<Doedsfall>.dødsfallSomJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "doedsdato" to it.doedsdato
        ))
    }
)

fun List<Foedselsdato>.fødselSomJsonArray() = JSONArray(
    map {
        JSONObject(mapOf(
            "foedselsdato" to it.foedselsdato
        ))
    }
)
