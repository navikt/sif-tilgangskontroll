package no.nav.siftilgangskontroll.tilgang

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.siftilgangskontroll.core.pdl.PdlService
import no.nav.siftilgangskontroll.core.tilgang.TilgangService
import no.nav.siftilgangskontroll.core.tilgang.TilgangsAttributter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TilgangServiceConfig {

    @Bean
    fun pdlService(pdlClient: GraphQLWebClient): PdlService = PdlService(pdlClient)

    @Bean
    fun tilgangsAttributter(pdlService: PdlService): TilgangsAttributter = TilgangsAttributter(pdlService)

    @Bean
    fun oppslagsService(tilgangsAttributter: TilgangsAttributter): TilgangService =
        TilgangService(tilgangsAttributter)
}
