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
import no.nav.siftilgangskontroll.pdl.generated.enums.ForelderBarnRelasjonRolle
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentperson.*
import no.nav.siftilgangskontroll.spesification.PolicyDecision
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation
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
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Folkeregisteridentifikator as FolkeregisteridentifikatorBarn
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
    fun `gitt NAV-bruker med adressebeskyttelse, forvent tilgang til barn med adressebeskyttelse`() {
        val relatertPersonsIdent = "123"

        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)),
            doedsfall = listOf(),
            forelderBarnRelasjon = listOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = relatertPersonsIdent,
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = null
                )
            )
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG)),
                    folkeregisteridentifikator = listOf(
                        FolkeregisteridentifikatorBarn(
                            relatertPersonsIdent
                        )
                    )
                ),
                code = "200"
            )
        )

        val policyEvaluation =
            tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel(listOf(relatertPersonsIdent)), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.PERMIT)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker uten adressebeskyttelse, forvent nektet tilgang til barn med adressebeskyttelse`() {
        val relatertPersonsIdent = "123"

        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf(),
            forelderBarnRelasjon = listOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = relatertPersonsIdent,
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = null
                )
            )
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG)),
                    folkeregisteridentifikator = listOf(FolkeregisteridentifikatorBarn(relatertPersonsIdent))
                ),
                code = "200"
            )
        )

        val policyEvaluation =
            tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel(listOf(relatertPersonsIdent)), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.DENY),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker med relasjoner, forvent nektet tilgang til ukjent barn`() {
        val ukjentRelasjonIdent = "456"

        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf(),
            forelderBarnRelasjon = listOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = "123",
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = null
                )
            )
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(),
                    folkeregisteridentifikator = listOf(FolkeregisteridentifikatorBarn(ukjentRelasjonIdent))
                ),
                code = "200"
            )
        )

        val policyEvaluation =
            tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel(listOf(ukjentRelasjonIdent)), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.DENY)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker uten adressebeskyttelse, forvent tilgang til barn uten adressebeskyttelse`() {
        val relatertPersonsIdent = "123"

        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf(),
            forelderBarnRelasjon = listOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = relatertPersonsIdent,
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = null
                )
            )
        )

        coEvery { pdlService.barn(any()) } returns listOf(
            HentPersonBolkResult(
                ident = "123456789",
                person = PersonBarn(
                    doedsfall = listOf(),
                    adressebeskyttelse = listOf(),
                    folkeregisteridentifikator = listOf(FolkeregisteridentifikatorBarn(relatertPersonsIdent))
                ),
                code = "200"
            )
        )

        val policyEvaluation =
            tilgangskontrollService.hentTilgangTilBarn(BarnTilgangForespørsel(listOf(relatertPersonsIdent)), jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.PERMIT)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker ikke lenger er i live, forvent nektet tilgang`() {

        coEvery { pdlService.person(any()) } returns Person(
            folkeregisteridentifikator = listOf(Folkeregisteridentifikator("123456789")),
            adressebeskyttelse = listOf(),
            doedsfall = listOf(Doedsfall(LocalDateTime.now().toString())),
            forelderBarnRelasjon = listOf()
        )

        val policyEvaluation = tilgangskontrollService.hentTilgangTilPerson(jwtToken)

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation.id).isEqualTo("FP.10")
        assertThat(policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
    }
}

private fun List<PolicyEvaluation>.resultat() =
    map {
        PolicyEvaluationResult(
            id = it.id,
            decision = it.decision
        )
    }
        .sortedBy { it.id }

private data class PolicyEvaluationResult(
    val id: String,
    val decision: PolicyDecision
)
