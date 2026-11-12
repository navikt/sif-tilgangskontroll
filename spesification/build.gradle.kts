plugins {
    kotlin("jvm")
}

val kotlinVersion: String by rootProject.extra
val kotlinXVersion: String by rootProject.extra
val orgJsonVersion: String by rootProject.extra

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinXVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")

    implementation("org.json:json:$orgJsonVersion")
}
