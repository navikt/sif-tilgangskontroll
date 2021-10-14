import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("plugin.jpa") version "1.5.31"
}

java.sourceCompatibility = JavaVersion.VERSION_11

val kotlinVersion by extra("1.5.31")
val kotlinXVersion by extra("1.5.2")
val springfoxVersion by extra("3.0.0")
val logstashLogbackEncoderVersion by extra("6.6")
val tokenSupportVersion by extra("1.3.8")
val springCloudVersion by extra("2020.0.3")
val retryVersion by extra("1.3.0")
val zalandoVersion by extra("0.26.2")
val awailitilityKotlinVersion by extra("4.1.0")
val assertkJvmVersion by extra("0.24")
val springMockkVersion by extra("3.0.1")
val mockkVersion by extra("1.11.0")
val guavaVersion by extra("23.0")
val okHttp3Version by extra("4.9.1")
val orgJsonVersion by extra("20210307")
val graphQLKotlinVersion by extra("4.2.0")
val jacksonKotlinModuleVersion by extra("2.12.5")

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
            jvmTarget = "11"
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

ext["okhttp3.version"] = okHttp3Version

dependencies {
    implementation(project(":spesification"))
    implementation(project(":core"))

    /*implementation("no.nav.sif.tilgangskontroll:spesification:1-25a6045")
    implementation("no.nav.sif.tilgangskontroll:core:1-25a6045")*/

    // NAV
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("com.squareup.okhttp3:okhttp:$okHttp3Version")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.retry:spring-retry:$retryVersion")
    implementation("org.springframework:spring-aspects")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit")
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    // Spring Cloud
    // https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-contract-stub-runner
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation("org.springframework.cloud:spring-cloud-starter")

    // SpringFox
    implementation("io.springfox:springfox-boot-starter:$springfoxVersion")

    // Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    //graphql
    implementation("com.expediagroup:graphql-kotlin-spring-client:$graphQLKotlinVersion")

    // Diverse
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.zalando:problem-spring-web-starter:$zalandoVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    testImplementation("org.awaitility:awaitility-kotlin:$awailitilityKotlinVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkJvmVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
