@file:Suppress("DEPRECATION")
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.hot.reload)
    alias(libs.plugins.ksp)
}

// Compose Resources: bundle the CJK fallback font (Noto Sans SC) consumed by the
// wasmJs build (see wasmJsMain/WebFonts.kt) so Skiko can render Chinese glyphs in
// the browser. Custom namespace keeps the generated `Res` accessor in our package.
compose.resources {
    publicResClass = true
    packageOfResClass = "xyz.junerver.composehooks"
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Web target: renders the same `App()` the android/desktop builds use inside a
    // browser canvas, making :app a four-platform sample (android + desktop + ios + web).
    // `binaries.executable()` registers the webpack run/distribution tasks
    // (wasmJsBrowserDevelopmentRun / wasmJsBrowserDistribution); without it only
    // wasmJsBrowserTest is available.
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeHooksWasm.js"
                // Workaround for `node:net` UnhandledSchemeError in the wasmJs bundle.
                //
                // kotlinx-datetime pulls in @js-joda/core, whose ESM build imports `node:net`;
                // webpack 5 rejects the `node:` scheme and aborts wasmJsBrowserDevelopmentWebpack.
                // Those imports only fire under Node.js (the libs feature-detect the environment
                // and skip them in a browser), so the snippet under this directory short-circuits
                // every `node:*` request to an empty module. Kotlin merges any *.js found here
                // into the generated webpack.config.js.
                configDirectory = project.layout.projectDirectory
                    .dir("src/wasmJsMain/webpack.config.d")
                    .asFile
            }
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlin.multiplatform.appdirs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.logback.classic)
            }
        }

        val commonIosAndroid by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.mmkv.kotlin)
            }
        }

        androidMain.get().dependsOn(commonIosAndroid)
        androidMain {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.mmkv.kotlin)
                implementation(libs.slf4j.android)
            }
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.material.icons.core)
                implementation(libs.material.icons.extended)
                implementation(libs.compose.lifecycle.viewmodel)
                implementation(libs.compose.lifecycle.viewmodel.compose)
                implementation(libs.compose.lifecycle.runtime.compose)

                implementation(libs.compose.navigation.compose)

                implementation(projects.hooks)
                implementation(projects.ai)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.bundles.ktor)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.schema.annotations)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.matching { it.name == "lintKotlinCommonMain" || it.name == "formatKotlinCommonMain" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

android {
    namespace = "xyz.junerver.composehooks"
    compileSdk = 36

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "xyz.junerver.composehooks"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "xyz.junerver.composehooks.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "xyz.junerver.composehooks"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Configure KSP arguments
ksp {
    arg("kotlinx.schema.withSchemaObject", "true")
    arg("kotlinx.schema.rootPackage", "xyz.junerver.composehooks")
}

// Add KSP processor for common target
dependencies {
    add("kspCommonMainMetadata", "org.jetbrains.kotlinx:kotlinx-schema-ksp:0.0.2")
}
