package no.nav.siftilgangskontroll.tilgang

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.siftilgangskontroll.core.pdl.PdlService
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TilgangServiceConfig {

    @Bean
    fun pdlService(pdlClient: GraphQLWebClient): PdlService = PdlService(pdlClient)

    @Bean
    fun oppslagsService(pdlService: PdlService): TilgangService = TilgangService(pdlService)
}
