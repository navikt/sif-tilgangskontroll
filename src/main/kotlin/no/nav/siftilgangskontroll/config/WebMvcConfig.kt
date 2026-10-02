package no.nav.siftilgangskontroll.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.cfg.DateTimeFeature

@Configuration
class WebMvcConfig() : WebMvcConfigurer {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebMvcConfigurer::class.java)
    }

    /**
     * Add handlers to serve static resources such as images, js, and, css
     * files from specific locations under web application root, the classpath,
     * and others.
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");

        super.addResourceHandlers(registry)
    }

    @Bean
    fun jacksonBuilderCustomizer(): JsonMapperBuilderCustomizer {
        log.info("-------> Customizing builder")
        return JsonMapperBuilderCustomizer { builder ->
            builder.disable(
                DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS,
                DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS
            )
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            builder.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
        }
    }
}
