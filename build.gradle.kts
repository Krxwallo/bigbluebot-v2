import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"

    application
}

group = "de.lookonthebrightsi"
version = "2.0.0"
application {
    mainClass.set("de.lookonthebrightsi.MainKt")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.1-RC1")
    implementation("dev.kord.x:emoji:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.slf4j:slf4j-simple:1.7.33")
    implementation("it.skrape:skrapeit:1.2.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.1.1")
    implementation("io.github.bonigarcia:webdrivermanager:5.0.3")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}