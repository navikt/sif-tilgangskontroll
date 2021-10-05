package no.nav.siftilgangskontroll.tilgang

import no.nav.siftilgangskontroll.core.pdl.PdlClientConfig
import no.nav.siftilgangskontroll.core.pdl.PdlService
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import no.nav.siftilgangskontroll.core.tilgang.TilgangsAttributter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TilgangServiceConfig(
    @Value("\${no.nav.gateways.pdl-api-base-url}") private val pdlBaseUrl: String
) {
    @Bean
    fun pdlClient() = PdlClientConfig(pdlBaseUrl)

    @Bean
    fun pdlService(pdlClient: PdlClientConfig): PdlService = PdlService(pdlClient)

    @Bean
    fun tilgangsAttributter(pdlService: PdlService): TilgangsAttributter = TilgangsAttributter(pdlService)

    @Bean
    fun oppslagsService(tilgangsAttributter: TilgangsAttributter): TilgangService =
        TilgangService(tilgangsAttributter)
}
