package no.nav.siftilgangskontroll

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [
    ErrorMvcAutoConfiguration::class
])
@EnableRetry
@EnableScheduling
@ConfigurationPropertiesScan("no.nav.siftilgangskontroll")
@EnableConfigurationProperties
class SifTilgangskontrollApplication

fun main(args: Array<String>) {
    runApplication<SifTilgangskontrollApplication>(*args)
}
