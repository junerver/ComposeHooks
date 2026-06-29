pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // Note: no repositoriesMode set. We deliberately do NOT use
    // FAIL_ON_PROJECT_REPOS (nor PREFER_SETTINGS) because the Kotlin wasmJs
    // target injects project-level Ivy repositories for the Node.js runtime
    // (org.nodejs:node at nodejs.org/dist) and Yarn (com.yarnpkg:yarn at
    // github.com/yarnpkg/yarn/releases) needed to run `wasmJsTest`. Any mode
    // that prefers/fails-on settings repositories breaks that injection.
    // The settings repositories below remain the primary source for normal
    // dependencies; project repositories are only used for these toolchains.
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ComposeHooks"
include(":app")
include(":hooks")
include(":ai")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
