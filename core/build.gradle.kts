
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql") version "7.1.4"
}

val tokenSupportVersion by extra("4.1.4")

val graphQLKotlinVersion by extra("7.0.2")

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion") {
        exclude("io.projectreactor.netty", "reactor-netty-http")
    }
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphQLKotlinVersion")  {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization")
    }

    constraints {
        implementation("com.fasterxml.jackson.core:jackson-databind") {
            because("https://github.com/navikt/sif-tilgangskontroll/security/dependabot/2")
            version {
                require("2.15.4")
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
