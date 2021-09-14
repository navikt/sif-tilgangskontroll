package no.nav.siftilgangskontroll.tilgang

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.siftilgangskontroll.util.personIdent
import org.springframework.stereotype.Service


@Service
class TilgangskontrollService(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val tokenValidationContextHolder: SpringTokenValidationContextHolder
) {

    fun harTilgang(): Tilgangskontroll {
        tokenValidationContextHolder.personIdent()
        return Tilgangskontroll()
    }
}

