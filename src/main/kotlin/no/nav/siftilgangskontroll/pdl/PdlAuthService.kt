package no.nav.siftilgangskontroll.pdl

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class PdlAuthService(
    oauth2Config: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {

    private val tokenxPdlClientProperties = oauth2Config.registration["tokenx-pdl-api"]
        ?: throw RuntimeException("could not find oauth2 client config for tokenx-pdl-api")

    private val azurePdlClientProperties = oauth2Config.registration["azure-pdl-api"]
        ?: throw RuntimeException("could not find oauth2 client config for azure-pdl-api")

    fun borgerToken(): String = oAuth2AccessTokenService.getAccessToken(tokenxPdlClientProperties).access_token
        ?: throw RuntimeException("Could not retrieve access token for tokenx-pdl-api")
    fun systemToken(): String = oAuth2AccessTokenService.getAccessToken(azurePdlClientProperties).access_token
        ?: throw RuntimeException("Could not retrieve access token for azure-pdl-api")
}
