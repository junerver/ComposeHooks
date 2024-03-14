plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

android {
    namespace = "xyz.junerver.compose.hooks"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

tasks.dokkaHtml  {
    outputDirectory.set(file("$rootDir/docs/api"))
    suppressInheritedMembers.set(true)
    moduleName.set("hooks")
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
    api(libs.ktx)
}
