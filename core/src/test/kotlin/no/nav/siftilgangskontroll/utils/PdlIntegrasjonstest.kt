package no.nav.siftilgangskontroll.utils

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.siftilgangskontroll.core.pdl.PdlService
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.http.client.reactive.JdkClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import java.net.http.HttpClient

/**
 * Kjører TilgangService mot PDL i WireMock, og utsteder tokens med MockOAuth2Server.
 * PDL-klienten settes opp slik konsumentene av biblioteket gjør det.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PdlIntegrasjonstest {

    protected val mockOAuth2Server = MockOAuth2Server()
    protected val wireMockServer = WireMockServer(options().dynamicPort())
    protected lateinit var tilgangService: TilgangService

    @BeforeAll
    fun startServere() {
        mockOAuth2Server.start()
        wireMockServer.start()

        val pdlClient = GraphQLWebClient(
            url = "${wireMockServer.baseUrl()}/pdl-api-mock/graphql",
            builder = WebClient.builder()
                .clientConnector(JdkClientHttpConnector(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .defaultHeader("Tema", "OMS")
        )
        tilgangService = TilgangService(PdlService(pdlClient))
    }

    @AfterEach
    fun nullstillWireMock() = wireMockServer.resetAll()

    @AfterAll
    fun stoppServere() {
        wireMockServer.stop()
        mockOAuth2Server.shutdown()
    }
}
