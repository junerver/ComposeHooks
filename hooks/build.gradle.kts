plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "xyz.junerver.compose.hooks"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

tasks.dokkaHtml  {
    outputDirectory.set(file("$rootDir/docs/api"))
    suppressInheritedMembers.set(true)
    moduleName.set("hooks")
}


dependencies {
    api(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.material)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose testing dependencies
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.test.manifest)

    // Compose
    api(libs.compose.ui)
    api(libs.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Kotlin and extension
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
    api(libs.ktx)
    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.collections.immutable)
}
