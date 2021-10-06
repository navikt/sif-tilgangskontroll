
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql")  version "4.2.0"
}

val tokenSupportVersion by extra("1.3.8")
val okHttp3Version by extra("4.9.1")
val graphQLKotlinVersion by extra("4.2.0")

ext["okhttp3.version"] = okHttp3Version

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")
}

tasks.withType<com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask> {
    queryFileDirectory.set("${project.projectDir}/src/main/resources/pdl")
    schemaFile.set(file("${project.projectDir}/src/main/resources/pdl/pdl-api-schema.graphql"))
    packageName.set("no.nav.siftilgangskontroll.pdl.generated")
}
