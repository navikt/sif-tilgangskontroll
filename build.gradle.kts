import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
}

java.sourceCompatibility = JavaVersion.VERSION_17

val kotlinVersion by extra("1.8.21")
val kotlinXVersion by extra("1.6.4")
val logstashLogbackEncoderVersion by extra("7.2")
val tokenSupportVersion by extra("3.1.0")
val springCloudVersion by extra("4.0.3")
val retryVersion by extra("2.0.0")
val awailitilityKotlinVersion by extra("4.2.0")
val assertkJvmVersion by extra("0.25")
val springMockkVersion by extra("3.1.1")
val mockkVersion by extra("1.13.2")
val guavaVersion by extra("31.1-jre")
val orgJsonVersion by extra("20230227")
val graphQLKotlinVersion by extra("7.0.1")
val jacksonKotlinModuleVersion by extra("2.15.0")

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    maven {
        name = "sif-tilgangskontroll"
        url = uri("https://maven.pkg.github.com/navikt/sif-tilgangskontroll")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

allprojects {
    group = "no.nav.sif.tilgangskontroll"

    repositories {
        mavenCentral()
    }

    afterEvaluate {
        dependencies {

            implementation("org.yaml:snakeyaml") {
                version {
                    strictly("2.0")
                    because("https://github.com/navikt/sif-tilgangskontroll/security/code-scanning/2")
                }
            }

            // Logging
            implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
            // Kotlin
            implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinXVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinXVersion")

            // Jackson
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinModuleVersion")

            implementation("org.json:json:$orgJsonVersion")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/navikt/sif-tilgangskontroll")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
        afterEvaluate {
            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                }
            }
        }
    }
}

dependencies {
    implementation(project(":spesification"))
    implementation(project(":core"))

    /*implementation("no.nav.sif.tilgangskontroll:spesification:1-25a6045")
    implementation("no.nav.sif.tilgangskontroll:core:1-25a6045")*/

    // NAV
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry:$retryVersion")
    implementation("org.springframework:spring-aspects")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit")
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    // Spring Cloud
    // https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-contract-stub-runner
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudVersion")

    // Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")

    // Diverse
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.google.guava:guava:$guavaVersion")
    testImplementation("org.awaitility:awaitility-kotlin:$awailitilityKotlinVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkJvmVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
