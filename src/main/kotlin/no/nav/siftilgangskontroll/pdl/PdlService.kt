package no.nav.siftilgangskontroll.pdl

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.siftilgangskontroll.pdl.generated.HentBarn
import no.nav.siftilgangskontroll.pdl.generated.HentPerson
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class PdlService(
    private val pdlClient: GraphQLWebClient,
    private val objectMapper: ObjectMapper,
    private val pdlAuthService: PdlAuthService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PdlService::class.java)
    }

    suspend fun person(ident: String): Person {
        val result = pdlClient.execute(HentPerson(HentPerson.Variables(ident))) {
            val borgerToken = pdlAuthService.borgerToken()
            header(HttpHeaders.AUTHORIZATION, "Bearer $borgerToken")
        }

        return when {
            !result.errors.isNullOrEmpty() -> {
                val errorSomJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.errors)
                logger.error("Feil ved henting av person. Årsak: {}", errorSomJson)
                throw IllegalStateException("Feil ved henting av person.")
            }
            result.data!!.hentPerson != null -> {
                val person = result.data!!.hentPerson!!
                logger.info("Hentet person: fra PDL: {}", person)
                person
            }
            else -> {
                throw IllegalStateException("Feil ved henting av person.")
            }
        }
    }

    suspend fun barn(identer: List<ID>): List<HentPersonBolkResult> {
        val result = pdlClient.execute(HentBarn(HentBarn.Variables(identer))) {
            header(HttpHeaders.AUTHORIZATION, "Bearer ${pdlAuthService.systemToken()}")
        }

        return when {
            !result.errors.isNullOrEmpty() -> {
                val errorSomJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.errors)
                logger.error("Feil ved henting av person-bolk. Årsak: {}", errorSomJson)
                throw IllegalStateException("Feil ved henting av person-bolk.")
            }
            result.data!!.hentPersonBolk.isNotEmpty() -> {
                val barn = result.data!!.hentPersonBolk
                    .filter { it.person!!.doedsfall.isEmpty() }

                logger.info("Hentet barn: fra PDL: {}", barn)
                barn
            }
            else -> {
                throw IllegalStateException("Feil ved henting av person-bolk.")
            }
        }
    }
}
