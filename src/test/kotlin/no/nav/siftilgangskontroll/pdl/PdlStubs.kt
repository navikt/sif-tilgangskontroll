package no.nav.siftilgangskontroll.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentperson.ForelderBarnRelasjon
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

internal fun WireMockServer.stubPdlHentPerson(requestBodyContaining: String, responseBody: () -> String) {
    stubFor(
        WireMock.post(WireMock.urlPathMatching("/pdl-api-mock/graphql"))
            .withHeader("Authorization", WireMock.matching(".*"))
            .withRequestBody(WireMock.containing(requestBodyContaining))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(responseBody.invoke())
            )
    )
}

internal fun hentPersonPdlResponse(
    personIdent: String,
    adressebeskyttelseGradering: AdressebeskyttelseGradering,
    forelderBarnRelasjon: String = forelderBarnPdlRelasjon(listOf()).toString()
): String = //language=json
    """
            {
                "data": {
                    "hentPerson": {
                        "adressebeskyttelse": [
                            {
                              "gradering": "${adressebeskyttelseGradering.name}"
                            }
                        ],
                        "folkeregisteridentifikator": [
                            {
                                "identifikasjonsnummer": "$personIdent"
                            }
                        ],
                        "doedsfall": [],
                        "forelderBarnRelasjon": $forelderBarnRelasjon
                    }
                }
            }
        """.trimIndent()

internal fun forelderBarnPdlRelasjon(relasjoner: List<ForelderBarnRelasjon>): JSONArray = JSONArray(relasjoner.map {
    JSONObject(
        mapOf(
            "relatertPersonsIdent" to it.relatertPersonsIdent,
            "relatertPersonsRolle" to it.relatertPersonsRolle,
            "minRolleForPerson" to it.minRolleForPerson,
        )
    )
})
