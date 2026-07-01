/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: Web font wiring for the wasmJs target.
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
package xyz.junerver.composehooks

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import xyz.junerver.composehooks.Res
import xyz.junerver.composehooks.noto_sans_sc
import org.jetbrains.compose.resources.Font

/**
 * Web font wiring for the wasmJs target.
 *
 * Skiko (the wasmJs canvas backend) does not fall back to the OS/browser font list the way the
 * JVM and Android backends do, so CJK glyphs render as tofu (□) unless a font covering them is
 * loaded explicitly. We bundle Noto Sans SC via Compose Resources (which handles the wasmJs
 * fetch for us) and apply it to every TextStyle.
 *
 * See https://github.com/JetBrains/compose-multiplatform/issues/3967.
 */

/** @return the [FontFamily] backed by the bundled Noto Sans SC. */
@Composable
internal fun webFontFamily(): FontFamily = FontFamily(
    Font(resource = Res.font.noto_sans_sc),
)

/**
 * Wraps [content] in a MaterialTheme whose typography copies every TextStyle to use [webFontFamily],
 * preserving all other style attributes (size, weight, line height, …) from the ambient theme.
 * [ComposeHooksTheme] inherits the ambient MaterialTheme.typography, so this is the single
 * injection point that covers the whole demo.
 */
@Composable
internal fun WithWebFont(content: @Composable () -> Unit) {
    val family = webFontFamily()
    val base = MaterialTheme.typography
    val withFont = Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
    MaterialTheme(typography = withFont, content = content)
}
