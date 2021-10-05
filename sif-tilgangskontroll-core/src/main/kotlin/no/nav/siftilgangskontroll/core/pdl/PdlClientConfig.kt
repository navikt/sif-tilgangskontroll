package no.nav.siftilgangskontroll.core.pdl

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import org.slf4j.LoggerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientRequest
import reactor.netty.http.client.HttpClientResponse

class PdlClientConfig(
   private val pdlBaseUrl: String,
   private val builder: WebClient.Builder = defaultWebClientBuilder()
) {

    val client: GraphQLWebClient = pdlClient()

    companion object {
        private val logger = LoggerFactory.getLogger(PdlClientConfig::class.java)

        fun defaultWebClientBuilder() = WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .doOnRequest { request: HttpClientRequest, _ ->
                            logger.info("{} {} {}", request.version(), request.method(), request.resourceUrl())
                        }
                        .doOnResponse { response: HttpClientResponse, _ ->
                            logger.info(
                                "{} - {} {} {}",
                                response.status().toString(),
                                response.version(),
                                response.method(),
                                response.resourceUrl()
                            )
                        }
                )
            )
            .defaultRequest {
                it.header("Tema", "OMS")
            }
    }

    private fun pdlClient() = GraphQLWebClient(
        url = "${pdlBaseUrl}/graphql",
        builder = builder
    )
}
