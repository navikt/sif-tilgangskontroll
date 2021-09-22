package no.nav.siftilgangskontroll.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
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

internal fun hentPersonPdlResponse(personIdent: String, adressebeskyttelseGradering: AdressebeskyttelseGradering): String = //language=json
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
                        "doedsfall": []
                    }
                }
            }
        """.trimIndent()
