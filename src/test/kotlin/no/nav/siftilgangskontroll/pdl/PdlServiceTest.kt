package no.nav.siftilgangskontroll.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"]
)
@AutoConfigureWireMock
@ActiveProfiles("test")
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test. Se application-test.yml -> no.nav.security.jwt.issuer.tokenx for konfigurasjon
internal class PdlServiceTest {

    @Autowired
    private lateinit var pdlService: PdlService

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @MockkBean
    private lateinit var oAuth2AccessTokenServiceMock: OAuth2AccessTokenService

    @BeforeEach
    fun setUp() {
        assertNotNull(wireMockServer)
        every { oAuth2AccessTokenServiceMock.getAccessToken(any()) } returns OAuth2AccessTokenResponse(
            "ey...", 299, 299, mutableMapOf()
        )
    }

    @Test
    fun `hentPerson happy case`() {
        val personIdent = "14026223262"
        wireMockServer.stubPdlHentPerson("hentPerson") {
            hentPersonPdlResponse(
                personIdent,
                AdressebeskyttelseGradering.STRENGT_FORTROLIG
            )
        }

        runBlocking { pdlService.person(personIdent) }
    }
}
