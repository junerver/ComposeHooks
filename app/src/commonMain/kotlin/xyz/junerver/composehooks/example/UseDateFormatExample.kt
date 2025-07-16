package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useDateFormat
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimestamp
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useDateFormat hook
  Author: Junerver
  Date: 2025/7/9-14:45
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useDateFormat hook
 */
@Composable
fun UseDateFormatExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useDateFormat Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo - similar to VueUse demo
        InteractiveDateFormatDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            val formattedDate by useDateFormat(
                date = now(),
                formatStr = "YYYY-MM-DD HH:mm:ss",
            )

            Text(text = "Current date and time: $formattedDate")
        }

        // Different format tokens
        ExampleCard(title = "Different Format Tokens") {
            val now = now()

            val format1 by useDateFormat(now, "YYYY-MM-DD")
            val format2 by useDateFormat(now, "MMM DD, YYYY")
            val format3 by useDateFormat(now, "dddd, MMMM DD, YYYY")
            val format4 by useDateFormat(now, "ddd, MMMM DD, YYYY")
            val format5 by useDateFormat(now, "dd, MMMM DD, YYYY")
            val format6 by useDateFormat(now, "d, MMMM DD, YYYY")
            val format7 by useDateFormat(now, "HH:mm:ss")
            val format8 by useDateFormat(now, "hh:mm:ss A")

            Column {
                FormatRow("YYYY-MM-DD", format1)
                FormatRow("MMM DD, YYYY", format2)
                FormatRow("dddd, MMMM DD, YYYY", format3)
                FormatRow("ddd, MMMM DD, YYYY", format4)
                FormatRow("dd, MMMM DD, YYYY", format5)
                FormatRow("d, MMMM DD, YYYY", format6)
                FormatRow("HH:mm:ss", format7)
                FormatRow("hh:mm:ss A", format8)
            }
        }

        // Custom locale example
        ExampleCard(title = "Custom Locale") {
            val now = now()

            val englishFormat by useDateFormat(now, "dddd, MMMM DD") {
                locale = "en-US"
            }

            val chineseFormat by useDateFormat(now, "dddd, MMMM DD") {
                locale = "zh-CN"
            }

            Column {
                Text("English: $englishFormat")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chinese: $chineseFormat")
            }
        }

        // Custom meridiem example
        ExampleCard(title = "Custom Meridiem") {
            val now = now()

            val customMeridiem by useDateFormat(now, "hh:mm:ss A") {
                customMeridiem = { hours, _, isLowercase, _ ->
                    if (hours > 11) {
                        if (isLowercase) "post meridiem" else "POST MERIDIEM"
                    } else {
                        if (isLowercase) "ante meridiem" else "ANTE MERIDIEM"
                    }
                }
            }

            Text("Custom meridiem: $customMeridiem")
        }

        // Timestamp example
        ExampleCard(title = "Using with Timestamp") {
            // Example timestamp (July 4, 2025, 12:30:45 PM)
            val timestamp = 1751889045000L

            val formattedTimestamp by useDateFormat(
                date = timestamp,
                formatStr = "MMMM DD, YYYY hh:mm:ss A",
            )

            Text("Formatted timestamp: $formattedTimestamp")
        }
    }
}

/**
 * Helper component for displaying a format and its result
 */
@Composable
private fun FormatRow(format: String, result: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = format,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = result,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Interactive demo component similar to VueUse demo
 */
@Composable
private fun InteractiveDateFormatDemo() {
    var formatter by useState("dddd YYYY-MM-DD HH:mm:ss")
    var selectedLocale by useState("en-US")

    val (timestamp) = useTimestamp()
    val formatted by useDateFormat(timestamp.value, formatter) {
        locale = selectedLocale
    }

    val localeOptions = listOf(
        "en-US" to "English (US)",
        "zh-CN" to "中国",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp), DividerDefaults.Thickness, DividerDefaults.color)

            // Display formatted date with large, bold, emerald-like styling
            Text(
                text = formatted,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Formatter Editor
            Text(
                text = "Formatter Editor :",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = formatter,
                onValueChange = { formatter = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                placeholder = { Text("Enter format string...") },
            )

            // Language selection
            Text(
                text = "Locale:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                localeOptions.forEach { (locale, label) ->
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = selectedLocale == locale,
                                onClick = { selectedLocale = locale },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedLocale == locale,
                            onClick = { selectedLocale = locale },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
