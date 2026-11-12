
plugins {
    kotlin("jvm")
    id("com.expediagroup.graphql") version "10.2.2"
}

val kotlinVersion: String by rootProject.extra
val kotlinXVersion: String by rootProject.extra
val orgJsonVersion: String by rootProject.extra

val tokenSupportVersion by extra("5.0.30")

val graphQLKotlinVersion by extra("10.2.2")
val springBootVersion by extra("4.0.9")
val junitVersion by extra("6.0.3")
val wiremockVersion by extra("3.13.2")
val jettyVersion by extra("12.1.14")
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
    // graphql-kotlin-spring-client drar inn Spring Boot 4.0.2, Spring 7.0.3 og Jackson 3.1.1, som har kjente sårbarheter.
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    // graphql-pluginen bruker også Jackson 3.1.1 når den genererer kode.
    listOf("graphqlClient", "graphqlGraalVM", "graphqlSDL").forEach {
        add(it, platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
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
    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    // Spring Boot-BOM-en styrer Jetty-kjernen, men ikke ee10-modulene som WireMock bruker.
    testImplementation(platform("org.eclipse.jetty.ee10:jetty-ee10-bom:$jettyVersion"))
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
