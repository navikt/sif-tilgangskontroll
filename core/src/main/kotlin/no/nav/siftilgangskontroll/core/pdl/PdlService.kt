package no.nav.siftilgangskontroll.core.pdl

import com.expediagroup.graphql.client.GraphQLClient
import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.*
import no.nav.siftilgangskontroll.pdl.generated.HentBarn
import no.nav.siftilgangskontroll.pdl.generated.HentIdent
import no.nav.siftilgangskontroll.pdl.generated.HentPerson
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.util.*

class PdlService(
    private val graphQLClient: GraphQLClient<*>,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PdlService::class.java)
        private const val NAV_CALL_ID = "Nav-Call-Id"
        private val objectMapper = jacksonObjectMapper()
    }

   internal suspend fun person(ident: String, borgerToken: String, callId: String): Person {
        val result = when(graphQLClient) {
            is GraphQLWebClient -> graphQLClient.execute(HentPerson(HentPerson.Variables(ident))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
                header(NAV_CALL_ID, callId)
            }
            is GraphQLKtorClient -> graphQLClient.execute(HentPerson(HentPerson.Variables(ident))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
                header(NAV_CALL_ID, callId)
            }
            else -> throw Exception("Instance of GraphQLClient is not supported")
        }

        return when {
            !result.errors.isNullOrEmpty() -> {
                val errorSomJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.errors)
                logger.error("Feil ved henting av person. Årsak: {}", errorSomJson)
                throw IllegalStateException("Feil ved henting av person.")
            }
            result.data!!.hentPerson != null ->  result.data!!.hentPerson!!
            else -> {
                throw IllegalStateException("Feil ved henting av person.")
            }
        }
    }

    internal suspend fun barn(identer: List<ID>, systemToken: String, callId: String = UUID.randomUUID().toString()): List<no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person> {
        val result = when(graphQLClient) {
            is GraphQLWebClient -> graphQLClient.execute(HentBarn(HentBarn.Variables(identer))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $systemToken")
                header(NAV_CALL_ID, callId)
            }
            is GraphQLKtorClient -> graphQLClient.execute(HentBarn(HentBarn.Variables(identer))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $systemToken")
                header(NAV_CALL_ID, callId)
            }
            else -> throw Exception("Instance of GraphQLClient is not supported")
        }

        return when {
            !result.errors.isNullOrEmpty() -> {
                val errorSomJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.errors)
                logger.error("Feil ved henting av person-bolk. Årsak: {}", errorSomJson)
                throw IllegalStateException("Feil ved henting av person-bolk.")
            }
            result.data!!.hentPersonBolk.isNotEmpty() -> result.data!!.hentPersonBolk.map { it.person!! }
            else -> {
                throw IllegalStateException("Feil ved henting av person-bolk.")
            }
        }
    }

    internal suspend fun aktørId(ident: String, borgerToken: String, callId: String = UUID.randomUUID().toString()): List<IdentInformasjon> {
        val result = when(graphQLClient) {
            is GraphQLWebClient -> graphQLClient.execute(HentIdent(HentIdent.Variables(ident, listOf(IdentGruppe.AKTORID)))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
                header(NAV_CALL_ID, callId)
            }
            is GraphQLKtorClient -> graphQLClient.execute(HentIdent(HentIdent.Variables(ident, listOf(IdentGruppe.AKTORID)))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
                header(NAV_CALL_ID, callId)
            }
            else -> throw Exception("Instance of GraphQLClient is not supported")
        }

        return when {
            !result.errors.isNullOrEmpty() -> {
                val errorSomJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.errors)
                logger.error("Feil ved henting av ident. Årsak: {}", errorSomJson)
                throw IllegalStateException("Feil ved henting av ident.")
            }
            !result.data!!.hentIdenter!!.identer.isNullOrEmpty() -> result.data!!.hentIdenter!!.identer
            else -> {
                throw IllegalStateException("Feil ved henting av ident.")
            }
        }
    }
}
