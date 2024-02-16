package no.nav.siftilgangskontroll.tilgang

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.siftilgangskontroll.core.behandling.Behandling
import no.nav.siftilgangskontroll.core.pdl.utils.PdlOperasjon
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentIdenterResponse
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentPersonBolkResponse
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentPersonResponse
import no.nav.siftilgangskontroll.core.tilgang.BarnTilgangForespørsel
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering.STRENGT_FORTROLIG
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentperson.*
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import no.nav.siftilgangskontroll.policy.spesification.PolicyEvaluation
import no.nav.siftilgangskontroll.utils.hentToken
import no.nav.siftilgangskontroll.wiremock.PdlResponses.defaultHentIdenterResult
import no.nav.siftilgangskontroll.wiremock.PdlResponses.defaultHentPersonBolkResult
import no.nav.siftilgangskontroll.wiremock.PdlResponses.defaultHentPersonResult
import no.nav.siftilgangskontroll.wiremock.stubPdlRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse as BarnAdressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Foedsel as BarnFødsel
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Folkeregisteridentifikator as BarnFolkeregisteridentifikator

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@ActiveProfiles("test")
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test.
@AutoConfigureWireMock
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Integrasjonstest - Kjører opp hele Spring Context med alle konfigurerte beans.
class TilgangServiceTest {

    @Autowired
    private lateinit var tilgangService: TilgangService

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var wireMockServer: WireMockServer

    private lateinit var jwtToken: String

    @BeforeEach
    internal fun setUp() {
        jwtToken = mockOAuth2Server.hentToken().serialize()
    }

    @Test
    fun `gitt NAV-bruker uten adressebeskyttelse, forvent nektet tilgang til barn med adressebeskyttelse`() {
        val relatertPersonsIdent = "123"

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    relatertPersonsIdent = relatertPersonsIdent
                )
            )
        }

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) {
            pdlHentPersonBolkResponse(
                personBolk = listOf(
                    defaultHentPersonBolkResult(
                        folkeregisteridentifikator = BarnFolkeregisteridentifikator(relatertPersonsIdent),
                        adressebeskyttelse = BarnAdressebeskyttelse(STRENGT_FORTROLIG)
                    )
                )
            )
        }

        val policyEvaluation =
            tilgangService.hentBarn(
                barnTilgangForespørsel = BarnTilgangForespørsel(
                    barnIdenter = listOf(relatertPersonsIdent)
                ),
                bearerToken = jwtToken,
                systemToken = jwtToken,
                behandling = Behandling.PLEIEPENGER_SYKT_BARN
            )

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.DENY),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.4", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker, forvent nektet tilgang til barn over myndighetsalder (18)`() {
        val relatertPersonsIdent = "123"

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    relatertPersonsIdent = relatertPersonsIdent
                )
            )
        }

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) {
            pdlHentPersonBolkResponse(
                personBolk = listOf(
                    defaultHentPersonBolkResult(
                        folkeregisteridentifikator = BarnFolkeregisteridentifikator(relatertPersonsIdent),
                        fødselsdato = BarnFødsel(foedselsdato = "2002-01-01", foedselsaar = 2002)
                    )
                )
            )
        }

        val policyEvaluation =
            tilgangService.hentBarn(
                barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(relatertPersonsIdent)),
                bearerToken = jwtToken,
                systemToken = jwtToken,
                behandling = Behandling.PLEIEPENGER_SYKT_BARN
            )

        assertThat(policyEvaluation).isNotNull()
        assertThat(policyEvaluation[0].policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(policyEvaluation[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.4", decision = PolicyDecision.DENY)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker med relasjoner, forvent nektet tilgang til ukjent barn`() {
        val ukjentRelasjonIdent = "456"

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    relatertPersonsIdent = "123"
                )
            )
        }

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) {
            pdlHentPersonBolkResponse(
                personBolk = listOf(
                    defaultHentPersonBolkResult(
                        folkeregisteridentifikator = BarnFolkeregisteridentifikator("456")
                    )
                )
            )
        }

        val barnOppslagRespons = tilgangService.hentBarn(
            barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(ukjentRelasjonIdent)),
            bearerToken = jwtToken,
            systemToken = jwtToken,
            behandling = Behandling.PLEIEPENGER_SYKT_BARN
        )

        assertThat(barnOppslagRespons).isNotNull()
        assertThat(barnOppslagRespons[0].policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(barnOppslagRespons[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.DENY),
                PolicyEvaluationResult(id = "SIF.4", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker uten adressebeskyttelse, forvent tilgang til barn uten adressebeskyttelse`() {
        val relatertPersonsIdent = "123"

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    relatertPersonsIdent = relatertPersonsIdent
                )
            )
        }

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) {
            pdlHentPersonBolkResponse(
                personBolk = listOf(
                    defaultHentPersonBolkResult(
                        folkeregisteridentifikator = BarnFolkeregisteridentifikator(relatertPersonsIdent)
                    )
                )
            )
        }

        val barnOppslagRespons =
            tilgangService.hentBarn(
                barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(relatertPersonsIdent)),
                bearerToken = jwtToken,
                systemToken = jwtToken,
                behandling = Behandling.PLEIEPENGER_SYKT_BARN
            )

        assertThat(barnOppslagRespons).isNotNull()
        assertThat(barnOppslagRespons[0].policyEvaluation.decision).isEqualTo(PolicyDecision.PERMIT)
        assertThat(barnOppslagRespons[0].policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "SIF.1", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.2", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.3", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "SIF.4", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker ikke lenger er i live, forvent nektet tilgang`() {
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    dødsdato = Doedsfall(LocalDate.now().toString())
                )
            )
        }

        val personOppslagRespons = tilgangService.hentPerson(
            bearerToken = jwtToken,
            behandling = Behandling.PLEIEPENGER_SYKT_BARN
        )

        assertThat(personOppslagRespons).isNotNull()
        assertThat(personOppslagRespons.policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(personOppslagRespons.policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "FP.10", decision = PolicyDecision.DENY),
                PolicyEvaluationResult(id = "FP.11", decision = PolicyDecision.PERMIT)
            )
        )
    }

    @Test
    fun `gitt NAV-bruker er under myndighetsalder (18), forvent nektet tilgang`() {

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            val fødselsdato = LocalDate.now().minusYears(17)
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    foedsel = Foedsel(fødselsdato.toString(), fødselsdato.year)
                )
            )
        }

        val personOppslagRespons = tilgangService.hentPerson(
            bearerToken = jwtToken,
            behandling = Behandling.PLEIEPENGER_SYKT_BARN
        )

        assertThat(personOppslagRespons).isNotNull()
        assertThat(personOppslagRespons.policyEvaluation.decision).isEqualTo(PolicyDecision.DENY)
        assertThat(personOppslagRespons.policyEvaluation.children.resultat()).isEqualTo(
            listOf(
                PolicyEvaluationResult(id = "FP.10", decision = PolicyDecision.PERMIT),
                PolicyEvaluationResult(id = "FP.11", decision = PolicyDecision.DENY)
            )
        )
    }

    @Test
    fun `test aktørId`() {

        val forventetAktørId = "123456"
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_IDENTER, medbehandlingsnummer = false) {
            pdlHentIdenterResponse(
                identer = defaultHentIdenterResult(forventetAktørId, IdentGruppe.AKTORID)
            )
        }

        val aktørId =
            tilgangService.hentAktørId(ident = "123", identGruppe = IdentGruppe.AKTORID, borgerToken = jwtToken)

        assertThat(aktørId).isNotNull()
        assertThat(aktørId.value).isEqualTo(forventetAktørId)
    }

    @Test
    fun `gitt forelderBarnRelasjon med relatertPersonsIdent lik null, forvent at den filtreres ut`() {
        val relatertPersonsIdent = null

        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(
                person = defaultHentPersonResult(
                    relatertPersonsIdent = relatertPersonsIdent
                )
            )
        }

        val forelderBarnRelasjonRelatertPersonIdent = tilgangService.hentPerson(
            bearerToken = jwtToken,
            behandling = Behandling.PLEIEPENGER_SYKT_BARN
        ).person!!.forelderBarnRelasjon.map { it.relatertPersonsIdent }
        assertThat(forelderBarnRelasjonRelatertPersonIdent).doesNotContain(null)
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
    val decision: PolicyDecision,
)
