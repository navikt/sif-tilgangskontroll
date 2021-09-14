package no.nav.siftilgangskontroll.utils

import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders

fun MockOAuth2Server.hentToken(
    subject: String = "12345678910",
    issuerId: String = "tokenx",
    claims: Map<String, String> = mapOf("acr" to "level4"),
    audience: String = "k9-sak-innsyn-api",
    expiry: Long = 3600
): SignedJWT = issueToken(issuerId = issuerId, subject = subject, claims = claims, audience = audience, expiry = expiry)

fun SignedJWT.tokenTilHttpEntity(): HttpEntity<String> {
    val headers = this.tokenTilHeader()
    return HttpEntity<String>(headers)
}

fun SignedJWT.tokenTilHeader(): HttpHeaders {
    val token = serialize()
    val headers = HttpHeaders()
    headers.setBearerAuth(token)
    return headers
}
