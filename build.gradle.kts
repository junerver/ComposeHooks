import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    apply(plugin = rootProject.libs.plugins.kotlinter.get().pluginId)

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
}
