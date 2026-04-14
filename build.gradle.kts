plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.1"
}

kotlin {
    jvmToolchain(21) // This tells Gradle: "Use the 64-bit JDK 21 we just installed"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Explicitly ask for the 64-bit Windows version
    implementation("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:1.6.1")

    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20231013")
}

compose.desktop {
    application {
        mainClass = "MainKt" // Ensure this matches your package structure

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "LlmOverlay"
            packageVersion = "1.0.0"
            description = "AI Assistant Overlay powered by Ollama"
            copyright = "© 2026 Liam"
            vendor = "uOttawa Students"
        }
    }
}