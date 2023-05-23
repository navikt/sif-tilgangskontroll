
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql") version "6.4.1"
}

val tokenSupportVersion by extra("3.0.12")

val graphQLKotlinVersion by extra("6.4.1")

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphQLKotlinVersion")  {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization")
    }

    constraints {
        implementation("com.fasterxml.jackson.core:jackson-databind") {
            because("https://github.com/navikt/sif-tilgangskontroll/security/dependabot/2")
            version {
                require("2.15.1")
            }
        }
        implementation("org.springframework:spring-core") {
            because("https://github.com/navikt/sif-tilgangskontroll/security/dependabot/4")
            version {
                require("6.0.8")
            }
        }
        implementation("org.springframework:spring-web") {
            because("https://github.com/navikt/sif-tilgangskontroll/security/dependabot/5")
            version {
                require("6.0.0")
            }
        }
        implementation("io.projectreactor.netty:reactor-netty-http") {
            because("https://github.com/navikt/sif-tilgangskontroll/security/dependabot/6")
            version {
                require("1.0.24")
            }
        }
    }
}

graphql {
    client {
        packageName = "no.nav.siftilgangskontroll.pdl.generated"
        schemaFile = file("${project.projectDir}/src/main/resources/pdl/pdl-api-schema.graphql")
        queryFileDirectory = "${project.projectDir}/src/main/resources/pdl"
    }
}
