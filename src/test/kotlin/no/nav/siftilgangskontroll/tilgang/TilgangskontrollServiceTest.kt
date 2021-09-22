package no.nav.siftilgangskontroll.tilgang

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.siftilgangskontroll.pdl.PdlService
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Doedsfall
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Folkeregisteridentifikator
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.spesification.PolicyDecision
import no.nav.siftilgangskontroll.utils.hentToken
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse as AdressebeskyttelseBarn
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person as PersonBarn

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@ActiveProfiles("test")
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Integrasjonstest - Kjører opp hele Spring Context med alle konfigurerte beans.
class TilgangskontrollServiceTest {

    @Autowired
    private lateinit var tilgangskontrollService: TilgangskontrollService

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    private lateinit var pdlService: PdlService

    private lateinit var jwtToken: JwtToken

    @BeforeEach
    internal fun setUp() {
        jwtToken = JwtToken(mockOAuth2Server.hentToken().serialize())
    }

    @Test
    fun `gitt skjermet borger, forvent tilgang til skjermet barn`() {
        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)),
            doedsfall = listOf()
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG))
                ),
                code = "200"
            )
        )

        val policyEvaluation = tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel("123"), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation.decision).isEqualTo(PolicyDecision.PERMIT)
        assertThat(policyEvaluation.reason).isEqualTo("Borger har tilgang til barn")
    }

    @Test
    fun `gitt vanlig borger, forvent tilgang forbudt til skjermet barn`() {
        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf()
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG))
                ),
                code = "200"
            )
        )

        val policyEvaluation = tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel("123"), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation.reason).isEqualTo("Borger har ikke tilgang til skjermet barn")
    }

    @Test
    fun `gitt vanlig borger, forvent tilgang til barn`() {
        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf()
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf()
                ),
                code = "200"
            )
        )

        val policyEvaluation = tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel("123"), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation.decision).isEqualTo(PolicyDecision.PERMIT)
        assertThat(policyEvaluation.reason).isEqualTo("Borger har tilgang til barn")
    }

    @Test
    fun `gitt borger ikke lenger er i live, forvent tilgang forbudt`() {
        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf(Doedsfall(LocalDateTime.now().toString()))
        )

        val policyEvaluation = tilgangskontrollService.hentTilgangTilPerson(jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation.reason).isEqualTo("Borger er ikke lenger i live")
    }
}
