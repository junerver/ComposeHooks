package xyz.junerver.compose.ai.http

/*
  Description: Global HTTP engine configuration
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Global configuration for HTTP engine.
 *
 * Use this to set a custom HTTP engine globally for all AI hooks.
 *
 * Example - Replace with OkHttp:
 * ```kotlin
 * // In your Application initialization
 * HttpEngineConfig.defaultEngineFactory = { OkHttpEngine(myOkHttpClient) }
 * ```
 *
 * Example - Add custom interceptors:
 * ```kotlin
 * HttpEngineConfig.defaultEngineFactory = {
 *     KtorHttpEngine(HttpClient {
 *         install(HttpTimeout) { ... }
 *         install(Logging) { ... }
 *     })
 * }
 * ```
 */
object HttpEngineConfig {
    /**
     * Factory function to create the default HTTP engine.
     *
     * Override this to use a custom HTTP engine globally.
     * Default: Creates a new [KtorHttpEngine] instance.
     */
    var defaultEngineFactory: () -> HttpEngine = { KtorHttpEngine() }
}
