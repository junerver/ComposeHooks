import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

allprojects {
    apply(plugin = rootProject.libs.plugins.kotlinter.get().pluginId)

    tasks.withType<KotlinCompile> {
        compilerOptions{
//            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        }
    }
}
