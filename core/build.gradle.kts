
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql")  version "6.4.0"
}

val tokenSupportVersion by extra("3.0.8")

val graphQLKotlinVersion by extra("6.4.0")

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphQLKotlinVersion")  {
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
