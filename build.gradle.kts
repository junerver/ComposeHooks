import org.gradle.api.tasks.SourceTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    if (path != ":app") {
        apply(plugin = rootProject.libs.plugins.kotlinter.get().pluginId)
    }

    tasks
        .matching { it.name.startsWith("lintKotlin") || it.name.startsWith("formatKotlin") }
        .configureEach {
            if (this is SourceTask) {
                exclude("**/build/**")
            }
        }
}
