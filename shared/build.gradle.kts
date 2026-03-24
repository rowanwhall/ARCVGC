import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sentryKmp)
    alias(libs.plugins.skie)
}

// Generate SentryConfig.kt from secrets.properties (gitignored)
abstract class GenerateSentryConfigTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    abstract val secretsFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val file = secretsFile.orNull?.asFile
        fun readSecret(key: String): String {
            if (file == null || !file.exists()) return ""
            return file.readLines()
                .filter { it.startsWith("$key=") }
                .map { it.substringAfter("=").trim() }
                .firstOrNull() ?: ""
        }

        val androidDsn = readSecret("SENTRY_DSN_ANDROID")
        val iosDsn = readSecret("SENTRY_DSN_IOS")
        val webDsn = readSecret("SENTRY_DSN_WEB")
        val dir = outputDir.get().asFile.resolve("com/arcvgc/app/data")
        dir.mkdirs()
        dir.resolve("SentryConfig.kt").writeText(
            """
            package com.arcvgc.app.data

            object SentryConfig {
                const val ANDROID_DSN: String = "$androidDsn"
                const val IOS_DSN: String = "$iosDsn"
                const val WEB_DSN: String = "$webDsn"
            }
            """.trimIndent()
        )
    }
}

val generateSentryConfig by tasks.registering(GenerateSentryConfigTask::class) {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        this.secretsFile.set(secretsFile)
    }
    outputDir.set(layout.buildDirectory.dir("generated/sentryConfig"))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    wasmJs {
        browser()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generateSentryConfig.map { layout.buildDirectory.dir("generated/sentryConfig").get() })
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = "com.arcvgc.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
