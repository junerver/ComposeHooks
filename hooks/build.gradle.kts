@file:Suppress("DEPRECATION")
@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
//    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(21)

    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexpect-actual-classes",
            "-Xconsistent-data-class-copy-visibility",
            "-opt-in=kotlin.time.ExperimentalTime"
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
            baseName = "hooks"
            isStatic = true
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.ui)
            //api(compose.foundation)
            //api(compose.material)
            //api(compose.components.resources)
            //api(compose.components.uiToolingPreview)
            implementation(libs.compose.lifecycle.runtime.compose)

            implementation(project.dependencies.platform(libs.kotlin.bom))
            api(libs.kotlin.stdlib)
            implementation(libs.kotlin.reflect)
            api(libs.kotlinx.coroutines)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.collections.immutable)

            api(libs.arrow.core)
            api(libs.arrow.functions)
        }

        val commonJvmAndroid by creating {
            dependsOn(commonMain.get())
        }

        androidMain.get().dependsOn(commonJvmAndroid)
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.biometric)
        }

        val desktopMain by getting {
            dependsOn(commonJvmAndroid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(compose.uiTest)
            implementation(compose.material3)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.compose.ui.test.junit4)
            implementation(compose.material3)
            implementation(libs.compose.ui.test.manifest)
        }
        val desktopTest by getting
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }
}

android {
    namespace = "xyz.junerver.compose.hooks"
    compileSdk = 36

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

//dokka {
//    dokkaPublications.html {
//        outputDirectory.set(file("$rootDir/docs/api"))
//    }
//    dokkaPublications.configureEach {
//        suppressInheritedMembers.set(true)
//    }
//    moduleName.set("hooks2")
//}

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
