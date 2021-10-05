package no.nav.siftilgangskontroll.core.utils

import no.nav.security.token.support.core.jwt.JwtToken

object TokenClaims {
    // Brukerident ligger i pid claim på tokenet for flyten idporten -> tokenx
    const val CLAIM_PID = "pid"

    // Brukerident ligger i sub claim på tokenet for flyten NAV loginservice -> tokenx
    const val CLAIM_SUB = "sub"
}

fun JwtToken.personIdent(): String {
    val pid = jwtTokenClaims.getStringClaim(TokenClaims.CLAIM_PID)
    val sub = jwtTokenClaims.getStringClaim(TokenClaims.CLAIM_SUB)

    return when {
        !pid.isNullOrBlank() -> pid
        !sub.isNullOrBlank() -> sub
        else -> throw IllegalStateException("Ugyldig token. Token inneholdt verken sub eller pid claim")
    }
}


