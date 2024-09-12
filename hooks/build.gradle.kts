import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
//    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexpect-actual-classes",
            "-Xconsistent-data-class-copy-visibility"
        )
    )

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        publishLibraryVariants("release")
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "hooks"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material)
            api(compose.ui)
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(project.dependencies.platform(libs.kotlin.bom))
            api(libs.kotlin.stdlib)
            implementation(libs.kotlin.reflect)
            api(libs.kotlinx.coroutines)
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)

            api(libs.arrow.core)
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

        iosMain.get().dependsOn(commonMain.get())
        iosX64Main.get().dependsOn(iosMain.get())
        iosArm64Main.get().dependsOn(iosMain.get())
        iosSimulatorArm64Main.get().dependsOn(iosMain.get())
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
//            implementation(libs.kotlin.test.junit)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

android {
    namespace = "xyz.junerver.compose.hooks"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


//tasks.dokkaHtml  {
//    outputDirectory.set(file("$rootDir/docs/api"))
//    suppressInheritedMembers.set(true)
//    moduleName.set("hooks")
//}


//dependencies {
//    api(platform(libs.compose.bom))
//    androidTestImplementation(platform(libs.compose.bom))
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.androidx.biometric)
//    implementation(libs.material)
//
//    // Testing dependencies
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//    // Compose testing dependencies
//    androidTestImplementation(libs.compose.ui.test)
//    androidTestImplementation(libs.compose.ui.test.junit4)
//    androidTestImplementation(libs.compose.material3)
//    debugImplementation(libs.compose.ui.test.manifest)
//
//    // Compose
//    api(libs.compose.ui)
//    api(libs.compose.foundation)
//    implementation(libs.androidx.lifecycle.runtime.compose)
//
//    // Kotlin and extension
//    implementation(platform(libs.kotlin.bom))
//    implementation(libs.kotlin.reflect)
//    api(libs.ktx)
//    api(libs.kotlinx.datetime)
//    implementation(libs.kotlinx.collections.immutable)
//}
