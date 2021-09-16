package no.nav.siftilgangskontroll.pdl

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.siftilgangskontroll.util.Constants.NAV_CALL_ID
import no.nav.siftilgangskontroll.util.MDCUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientRequest
import reactor.netty.http.client.HttpClientResponse

@Configuration
class PdlClientConfig(
    oauth2Config: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    @Value("\${no.nav.gateways.pdl-api-base-url}") private val pdlBaseUrl: String
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PdlClientConfig::class.java)
    }

    private val tokenxPdlClientProperties = oauth2Config.registration["tokenx-pdl-api"]
        ?: throw RuntimeException("could not find oauth2 client config for tokenx-pdl-api")

    @Bean
    fun pdlClient() = GraphQLWebClient(
        url = "${pdlBaseUrl}/graphql",
        builder = WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .doOnRequest { request: HttpClientRequest, _ ->
                            logger.info("{} {} {}", request.version(), request.method(), request.resourceUrl())
                        }
                        .doOnResponse { response: HttpClientResponse, _ ->
                            logger.info("{} - {} {} {}", response.status().toString(), response.version(), response.method(), response.resourceUrl())
                        }
                )
            )
            .defaultRequest {
                it.header(NAV_CALL_ID, MDCUtil.callIdOrNew())
                it.header(
                    AUTHORIZATION,
                    "Bearer ${oAuth2AccessTokenService.getAccessToken(tokenxPdlClientProperties).accessToken}"
                )
            }
    )
}
