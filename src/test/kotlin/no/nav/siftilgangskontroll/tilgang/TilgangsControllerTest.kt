package no.nav.siftilgangskontroll.tilgang

import com.ninjasquad.springmockk.MockkBean
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.siftilgangskontroll.Routes.BARN
import no.nav.siftilgangskontroll.Routes.TILGANG
import no.nav.siftilgangskontroll.config.SecurityConfiguration
import no.nav.siftilgangskontroll.util.CallIdGenerator
import no.nav.siftilgangskontroll.utils.hentToken
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.servlet.http.Cookie


@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test.
@Import(CallIdGenerator::class, SecurityConfiguration::class)
@WebMvcTest(controllers = [TilgangsController::class])
@ActiveProfiles("test")
class TilgangsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean(relaxed = true)
    lateinit var tilgangskontrollService: TilgangskontrollService

    @BeforeAll
    internal fun setUp() {
        assertNotNull(mockOAuth2Server)
    }

    @Test
    fun `gitt request uten token, forevnt 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("$TILGANG$BARN")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value("/problem-details/uautentisert-forespørsel"))
            .andExpect(jsonPath("$.title").value("Ikke autentisert"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("no.nav.security.token.support.core.exceptions.JwtTokenMissingException: no valid token found in validation context"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
    }

    @Test
    fun `gitt request med token utsedt av annen issuer, forevnt 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("$TILGANG$BARN")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(
                    Cookie(
                        "selvbetjening-idtoken",
                        mockOAuth2Server.hentToken(issuerId = "ukjent issuer").serialize()
                    )
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value("/problem-details/uautentisert-forespørsel"))
            .andExpect(jsonPath("$.title").value("Ikke autentisert"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("no.nav.security.token.support.core.exceptions.JwtTokenMissingException: no valid token found in validation context"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
    }

    @Test
    fun `gitt request med token med ukjent audience, forevnt 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("$TILGANG$BARN")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(
                    Cookie(
                        "selvbetjening-idtoken",
                        mockOAuth2Server.hentToken(audience = "ukjent audience").serialize()
                    )
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value("/problem-details/uautentisert-forespørsel"))
            .andExpect(jsonPath("$.title").value("Ikke autentisert"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("no.nav.security.token.support.core.exceptions.JwtTokenMissingException: no valid token found in validation context"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
    }
}
