@file:Suppress("DEPRECATION")
@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(21)

    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexpect-actual-classes",
            "-Xconsistent-data-class-copy-visibility",
        )
    )

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ai"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // Compose
            api(compose.runtime)
            api(compose.ui)

            // Hooks module
            api(project(":hooks"))

            // Kotlin
            implementation(project.dependencies.platform(libs.kotlin.bom))
            api(libs.kotlin.stdlib)

            // Coroutines
            api(libs.kotlinx.coroutines)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // DateTime (for multi-provider metrics)
            implementation(libs.kotlinx.datetime)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiat)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        val commonJvmAndroid by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        androidMain.get().dependsOn(commonJvmAndroid)
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        val desktopMain by getting {
            dependsOn(commonJvmAndroid)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(compose.uiTest)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "xyz.junerver.compose.ai"
    compileSdk = 36

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.register("verifyCoverageBaseline") {
    group = "verification"
    description = "Verify minimum line coverage from Kover XML report."
    dependsOn("koverXmlReport")

    doLast {
        val candidates =
            listOf(
                layout.buildDirectory.file("reports/kover/report.xml").get().asFile,
                layout.buildDirectory.file("reports/kover/xml/report.xml").get().asFile,
                layout.buildDirectory.file("reports/kover/xmlReport.xml").get().asFile,
            )
        val reportFile =
            candidates.firstOrNull { it.exists() }
                ?: error("Cannot find Kover XML report. Tried: ${candidates.joinToString()}")

        val documentBuilderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val document = documentBuilderFactory.newDocumentBuilder().parse(reportFile)
        val report = document.documentElement
        require(report.nodeName == "report") { "Unexpected Kover XML root: ${report.nodeName}" }

        var lineCounter: org.w3c.dom.Element? = null
        val children = report.childNodes
        for (index in 0 until children.length) {
            val node = children.item(index) as? org.w3c.dom.Element ?: continue
            if (node.tagName != "counter") continue
            if (node.getAttribute("type") != "LINE") continue
            lineCounter = node
            break
        }
        val counter =
            lineCounter
                ?: error("Cannot find LINE counter in Kover report root: $reportFile")

        val covered = counter.getAttribute("covered").toLong()
        val missed = counter.getAttribute("missed").toLong()

        val total = covered + missed
        require(total > 0) { "Line coverage is empty in report: $reportFile" }
        val coveragePercent = covered * 100.0 / total
        val minimumPercent = 80.0
        if (coveragePercent < minimumPercent) {
            error(
                "Line coverage %.2f%% is below required %.2f%%."
                    .format(coveragePercent, minimumPercent),
            )
        }
        logger.lifecycle(
            "Line coverage %.2f%% (threshold %.2f%%)"
                .format(coveragePercent, minimumPercent),
        )
    }
}

tasks.register("runCoverageChecks") {
    group = "verification"
    description = "Run tests and validate minimum code coverage baseline."
    dependsOn("allTests", "koverXmlReport", "verifyCoverageBaseline")
}
