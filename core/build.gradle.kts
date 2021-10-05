import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql")  version "4.2.0"
}

java.sourceCompatibility = JavaVersion.VERSION_11

val logstashLogbackEncoderVersion by extra("6.6")
val tokenSupportVersion by extra("1.3.8")
val okHttp3Version by extra("4.9.1")
val graphQLKotlinVersion by extra("4.2.0")

ext["okhttp3.version"] = okHttp3Version

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType<com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask> {
    queryFileDirectory.set("${project.projectDir}/src/main/resources/pdl")
    schemaFile.set(file("${project.projectDir}/src/main/resources/pdl/pdl-api-schema.graphql"))
    packageName.set("no.nav.siftilgangskontroll.pdl.generated")
}
