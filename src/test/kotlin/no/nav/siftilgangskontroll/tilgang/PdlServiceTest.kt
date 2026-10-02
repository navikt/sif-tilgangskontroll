package no.nav.siftilgangskontroll.tilgang

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.siftilgangskontroll.core.behandling.Behandling
import no.nav.siftilgangskontroll.core.pdl.utils.PdlOperasjon
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentIdenterBolkResponse
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentPersonBolkResponse
import no.nav.siftilgangskontroll.core.pdl.utils.pdlHentPersonResponse
import no.nav.siftilgangskontroll.core.tilgang.BarnTilgangForespørsel
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.HentIdenterBolkResult
import no.nav.siftilgangskontroll.policy.spesification.PolicyDecision
import no.nav.siftilgangskontroll.utils.hentToken
import no.nav.siftilgangskontroll.wiremock.PdlResponses.defaultHentPersonBolkResult
import no.nav.siftilgangskontroll.wiremock.PdlResponses.defaultHentPersonResult
import no.nav.siftilgangskontroll.wiremock.stubPdlRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock
import java.util.*
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse as BarnAdressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Folkeregisteridentifikator as BarnFolkeregisteridentifikator
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.IdentInformasjon as BolkIdentInformasjon

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@ActiveProfiles("test")
@EnableMockOAuth2Server
@EnableWireMock(ConfigureWireMock(name = "pdl-api", portProperties = ["wiremock.server.port"]))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PdlServiceTest {

    @Autowired
    private lateinit var tilgangService: TilgangService

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @InjectWireMock("pdl-api")
    private lateinit var wireMockServer: WireMockServer

    private lateinit var jwtToken: String

    @BeforeEach
    internal fun setUp() {
        jwtToken = mockOAuth2Server.hentToken(subject = BRUKER_IDENT).serialize()
    }

    @ParameterizedTest
    @ValueSource(strings = ["FORTROLIG", "STRENGT_FORTROLIG", "STRENGT_FORTROLIG_UTLAND", "UGRADERT", "NY_UKJENT_GRADERING"])
    fun `barn med adressebeskyttelse nektes tilgang, også ved UGRADERT og ukjent gradering`(gradering: String) {
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(defaultHentPersonResult(ident = BRUKER_IDENT, relatertPersonsIdent = BARN_IDENT))
        }
        val barnBolk = pdlHentPersonBolkResponse(
            personBolk = listOf(
                defaultHentPersonBolkResult(
                    folkeregisteridentifikator = BarnFolkeregisteridentifikator(BARN_IDENT),
                    adressebeskyttelse = BarnAdressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
                )
            )
        ).replace("\"STRENGT_FORTROLIG\"", "\"$gradering\"")
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) { barnBolk }

        val respons = tilgangService.hentBarn(
            barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(BARN_IDENT)),
            bearerToken = jwtToken,
            systemToken = jwtToken,
            behandling = Behandling.PLEIEPENGER_SYKT_BARN
        )

        assertThat(respons).hasSize(1)
        assertThat(respons[0].barn).isNull()
        assertThat(respons[0].policyEvaluation.children.first { it.id == "SIF.1" }.decision)
            .isEqualTo(PolicyDecision.DENY)
    }

    @Test
    fun `hentPerson sender token, callId og behandlingsnummer til PDL`() {
        val callId = UUID.randomUUID().toString()
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON) {
            pdlHentPersonResponse(defaultHentPersonResult(ident = BRUKER_IDENT))
        }

        tilgangService.hentPerson(bearerToken = jwtToken, callId = callId, behandling = Behandling.PLEIEPENGER_SYKT_BARN)

        verifiserPdlKall(
            operasjon = PdlOperasjon.HENT_PERSON,
            callId = callId,
            behandlingsnummer = Behandling.PLEIEPENGER_SYKT_BARN.behandlingsnummer,
            variabel = "$.variables.ident" to BRUKER_IDENT
        )
    }

    @Test
    fun `slåOppBarn sender token, callId og behandlingsnummer til PDL`() {
        val callId = UUID.randomUUID().toString()
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_PERSON_BOLK) {
            pdlHentPersonBolkResponse(listOf(defaultHentPersonBolkResult(folkeregisteridentifikator = BarnFolkeregisteridentifikator(BARN_IDENT))))
        }

        tilgangService.slåOppBarn(
            barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(BARN_IDENT)),
            systemToken = jwtToken,
            callId = callId,
            behandling = Behandling.OMSORGSPENGERUTBETALING
        )

        verifiserPdlKall(
            operasjon = PdlOperasjon.HENT_PERSON_BOLK,
            callId = callId,
            behandlingsnummer = Behandling.OMSORGSPENGERUTBETALING.behandlingsnummer,
            variabel = "$.variables.identer[0]" to BARN_IDENT
        )
    }

    @Test
    fun `hentIdenter henter identer i bolk`() {
        val callId = UUID.randomUUID().toString()
        val forventet = listOf(
            HentIdenterBolkResult(
                ident = BRUKER_IDENT,
                identer = listOf(BolkIdentInformasjon(ident = "2000000000001", gruppe = IdentGruppe.AKTORID)),
                code = "ok"
            ),
            HentIdenterBolkResult(ident = BARN_IDENT, identer = null, code = "not_found")
        )
        wireMockServer.stubPdlRequest(PdlOperasjon.HENT_IDENTER_BOLK, medbehandlingsnummer = false) {
            pdlHentIdenterBolkResponse(forventet)
        }

        val respons = tilgangService.hentIdenter(
            identer = listOf(BRUKER_IDENT, BARN_IDENT),
            identGrupper = listOf(IdentGruppe.AKTORID),
            systemToken = jwtToken,
            callId = callId
        )

        assertThat(respons).isEqualTo(forventet)
        verifiserPdlKall(
            operasjon = PdlOperasjon.HENT_IDENTER_BOLK,
            callId = callId,
            behandlingsnummer = null,
            variabel = "$.variables.identer[1]" to BARN_IDENT
        )
    }

    @ParameterizedTest
    @EnumSource(PdlOperasjon::class)
    fun `feil fra PDL gir IllegalStateException`(operasjon: PdlOperasjon) {
        wireMockServer.stubPdlRequest(operasjon, medbehandlingsnummer = false) { pdlFeilrespons(operasjon.navn) }

        val (kall, melding) = when (operasjon) {
            PdlOperasjon.HENT_PERSON -> Pair(
                { tilgangService.hentPerson(bearerToken = jwtToken, behandling = Behandling.PLEIEPENGER_SYKT_BARN) },
                "Feil ved henting av person."
            )
            PdlOperasjon.HENT_PERSON_BOLK -> Pair(
                {
                    tilgangService.slåOppBarn(
                        barnTilgangForespørsel = BarnTilgangForespørsel(barnIdenter = listOf(BARN_IDENT)),
                        systemToken = jwtToken,
                        behandling = Behandling.PLEIEPENGER_SYKT_BARN
                    )
                },
                "Feil ved henting av person-bolk."
            )
            PdlOperasjon.HENT_IDENTER -> Pair(
                { tilgangService.hentAktørId(ident = BRUKER_IDENT, identGruppe = IdentGruppe.AKTORID, borgerToken = jwtToken) },
                "Feil ved henting av ident."
            )
            PdlOperasjon.HENT_IDENTER_BOLK -> Pair(
                {
                    tilgangService.hentIdenter(
                        identer = listOf(BRUKER_IDENT),
                        identGrupper = listOf(IdentGruppe.AKTORID),
                        systemToken = jwtToken
                    )
                },
                "Feil ved henting av identer."
            )
        }

        val feil = assertThrows<IllegalStateException> { kall() }
        assertThat(feil).hasMessage(melding)
    }

    private fun verifiserPdlKall(
        operasjon: PdlOperasjon,
        callId: String,
        behandlingsnummer: String?,
        variabel: Pair<String, String>,
    ) {
        val forventetKall = WireMock.postRequestedFor(WireMock.urlPathEqualTo("/pdl-api-mock/graphql"))
            .withHeader("Authorization", WireMock.equalTo("Bearer $jwtToken"))
            .withHeader("Nav-Call-Id", WireMock.equalTo(callId))
            .withHeader("Tema", WireMock.equalTo("OMS"))
            .withRequestBody(WireMock.containing(operasjon.navn))
            .withRequestBody(WireMock.matchingJsonPath(variabel.first, WireMock.equalTo(variabel.second)))
        when (behandlingsnummer) {
            null -> forventetKall.withoutHeader("Behandlingsnummer")
            else -> forventetKall.withHeader("Behandlingsnummer", WireMock.equalTo(behandlingsnummer))
        }
        wireMockServer.verify(1, forventetKall)
    }

    private fun pdlFeilrespons(operasjon: String) =
        //language=json
        """
        {
          "errors": [
            {
              "message": "Ikke tilgang til å se person",
              "locations": [{"line": 2, "column": 5}],
              "path": ["$operasjon"],
              "extensions": {"code": "unauthorized", "classification": "ExecutionAborted"}
            }
          ],
          "data": null
        }
        """.trimIndent()

    private companion object {
        const val BRUKER_IDENT = "12345678910"
        const val BARN_IDENT = "123"
    }
}
