
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql") version "10.2.2"
}

val kotlinVersion: String by rootProject.extra
val kotlinXVersion: String by rootProject.extra
val orgJsonVersion: String by rootProject.extra

val tokenSupportVersion by extra("5.0.30")

val graphQLKotlinVersion by extra("10.2.2")
val junitVersion by extra("6.0.3")
val wiremockVersion by extra("3.13.2")
val mockOAuth2ServerVersion by extra("3.0.1")
val assertkJvmVersion by extra("0.25")

dependencies {
    implementation(project(":spesification"))

    // NAV
    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion") {
        exclude("io.projectreactor.netty", "reactor-netty-http")
    }

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinXVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")

    implementation("org.json:json:$orgJsonVersion")

    // Test
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.wiremock:wiremock:$wiremockVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkJvmVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

graphql {
    client {
        packageName = "no.nav.siftilgangskontroll.pdl.generated"
        schemaFile = file("${project.projectDir}/src/main/resources/pdl/pdl-api-schema.graphql")
        queryFileDirectory = "${project.projectDir}/src/main/resources/pdl"
    }
}
