package no.nav.siftilgangskontroll.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.siftilgangskontroll.core.pdl.utils.PdlOperasjon
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

internal fun WireMockServer.stubPdlRequest(pdlOperasjon: PdlOperasjon, medbehandlingsnummer: Boolean = true, responseBody: () -> String) {
    val mappingBuilder = WireMock.post(WireMock.urlPathMatching("/pdl-api-mock/graphql"))
        .withHeader("Authorization", WireMock.matching(".*"))
    if (medbehandlingsnummer) {
        mappingBuilder.withHeader("Behandlingsnummer", WireMock.matching(".*"))
    }

    stubFor(
        mappingBuilder
            .withRequestBody(WireMock.containing(pdlOperasjon.navn))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(responseBody.invoke())
            )
    )
}
