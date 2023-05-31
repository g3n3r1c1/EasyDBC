import kotlin.script.experimental.jvm.util.classpathFromClass

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "kt.dbc"
version = "Alpha-0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.20-RC")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20-RC")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

sourceSets.main {
    java.srcDirs( "src/main/kotlin")
    kotlin.srcDirs("src/main/kotlin")
}



application {
    mainClass.set("MainKt")
}