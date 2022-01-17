
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql")  version "5.3.2"
}

val tokenSupportVersion by extra("1.3.8")
val okHttp3Version by extra("4.9.1")
val graphQLKotlinVersion by extra("5.1.1")

ext["okhttp3.version"] = okHttp3Version

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

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
