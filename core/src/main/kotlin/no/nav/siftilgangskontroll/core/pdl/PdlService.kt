package no.nav.siftilgangskontroll.core.pdl

import com.expediagroup.graphql.client.GraphQLClient
import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.*
import no.nav.siftilgangskontroll.pdl.generated.HentBarn
import no.nav.siftilgangskontroll.pdl.generated.HentPerson
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

class PdlService(
    private val graphQLClient: GraphQLClient<*>,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PdlService::class.java)

        private val objectMapper = jacksonObjectMapper()
    }

   internal suspend fun person(ident: String, borgerToken: String): Person {
        val result = when(graphQLClient) {
            is GraphQLWebClient -> graphQLClient.execute(HentPerson(HentPerson.Variables(ident))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
            }
            is GraphQLKtorClient -> graphQLClient.execute(HentPerson(HentPerson.Variables(ident))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
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

    internal suspend fun barn(identer: List<ID>, systemToken: String): List<no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person> {
        val result = when(graphQLClient) {
            is GraphQLWebClient -> graphQLClient.execute(HentBarn(HentBarn.Variables(identer))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $systemToken")
            }
            is GraphQLKtorClient -> graphQLClient.execute(HentBarn(HentBarn.Variables(identer))) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $systemToken")
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
}
