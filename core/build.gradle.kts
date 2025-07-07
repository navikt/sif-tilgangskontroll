
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql") version "8.8.0"
}

val tokenSupportVersion by extra("5.0.30")

val graphQLKotlinVersion by extra("8.8.1")

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion") {
        exclude("io.projectreactor.netty", "reactor-netty-http")
    }
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphQLKotlinVersion") {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization")
    }

}

graphql {
    client {
        packageName = "no.nav.siftilgangskontroll.pdl.generated"
        schemaFile = file("${project.projectDir}/src/main/resources/pdl/pdl-api-schema.graphql")
        queryFileDirectory = "${project.projectDir}/src/main/resources/pdl"
    }
}
