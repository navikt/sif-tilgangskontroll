
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql")  version "6.3.3"
}

val tokenSupportVersion by extra("2.1.6")
val okHttp3Version by extra("4.10.0")
val graphQLKotlinVersion by extra("6.2.5")

ext["okhttp3.version"] = okHttp3Version

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
