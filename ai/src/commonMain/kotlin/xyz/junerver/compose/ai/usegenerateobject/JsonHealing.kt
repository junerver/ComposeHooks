package xyz.junerver.compose.ai.usegenerateobject

/*
  Description: JSON healing utilities for handling malformed AI responses
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0

  Provides multi-stage JSON repair:
  1. Extract JSON from text (remove markdown, find JSON boundaries)
  2. Clean format issues (comments, quotes, trailing commas)
  3. Repair structure (balance brackets)
*/

/**
 * Main entry point for JSON healing.
 *
 * Applies multi-stage repair to handle common issues in AI-generated JSON:
 * - Embedded JSON in text
 * - Markdown code blocks
 * - Comments (// and /* */)
 * - Single quotes instead of double quotes
 * - Trailing commas
 * - Incomplete JSON (missing closing brackets)
 *
 * @param raw The raw response text from AI
 * @param enableHealing Whether to enable healing (default true)
 * @return Repaired JSON string ready for parsing
 */
internal fun healJson(raw: String, enableHealing: Boolean = true): String {
    if (!enableHealing) {
        return cleanJsonResponse(raw) // Fallback to simple cleaning
    }

    if (raw.isBlank()) {
        return raw
    }

    var json = raw
    json = extractJson(json) // Stage 1: Extract JSON
    json = cleanJsonFormat(json) // Stage 2: Clean format
    json = repairJsonStructure(json) // Stage 3: Repair structure
    return json
}

/**
 * Cleans up potential markdown code blocks from JSON response.
 */
internal fun cleanJsonResponse(raw: String): String {
    var result = raw.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    // Remove trailing commas
    result = result.replace(Regex(",\\s*}"), "}")
    result = result.replace(Regex(",\\s*]"), "]")

    return result
}

/**
 * Stage 1: Extract JSON from text.
 *
 * Handles:
 * - Markdown code blocks (```json ... ```)
 * - Embedded JSON in text ("Here's the result: {...}")
 *
 * @param text The raw text containing JSON
 * @return Extracted JSON string
 */
private fun extractJson(text: String): String {
    // 1. Remove markdown code blocks
    var cleaned = cleanJsonResponse(text)

    // 2. Find JSON boundaries using proper bracket matching
    val firstBrace = cleaned.indexOfAny(charArrayOf('{', '['))
    if (firstBrace < 0) return cleaned

    // Find matching closing bracket
    var depth = 0
    var inString = false
    var escapeNext = false
    var lastBrace = -1

    for (i in firstBrace until cleaned.length) {
        val char = cleaned[i]
        when {
            escapeNext -> escapeNext = false
            char == '\\' && inString -> escapeNext = true
            char == '"' -> inString = !inString
            !inString -> {
                when (char) {
                    '{', '[' -> depth++
                    '}', ']' -> {
                        depth--
                        if (depth == 0) {
                            lastBrace = i
                            break
                        }
                    }
                }
            }
        }
    }

    // If we found a matching bracket, extract up to it
    // Otherwise, extract everything from firstBrace to end (incomplete JSON)
    cleaned = if (lastBrace > firstBrace) {
        cleaned.substring(firstBrace, lastBrace + 1)
    } else {
        cleaned.substring(firstBrace)
    }

    return cleaned
}

/**
 * Stage 2: Clean format issues.
 *
 * Applies multiple cleaning operations:
 * - Remove comments
 * - Fix quotes
 * - Remove trailing commas
 *
 * @param json The JSON string to clean
 * @return Cleaned JSON string
 */
private fun cleanJsonFormat(json: String): String {
    var result = json
    result = removeComments(result)
    result = fixQuotes(result)
    result = removeTrailingCommas(result)
    result = compactWhitespace(result)
    return result
}

/**
 * Remove comments from JSON.
 *
 * Handles:
 * - Single-line comments: // ...
 * - Multi-line comments: /* ... */
 *
 * @param json The JSON string
 * @return JSON without comments
 */
private fun removeComments(json: String): String {
    var result = json
    // Remove single-line comments // and the newline after them
    result = result.replace(Regex("//[^\n]*\n"), "")
    // Remove single-line comments at end of string (no newline after)
    result = result.replace(Regex("//[^\n]*$"), "")
    // Remove multi-line comments /* */ (use [\s\S] for KMP compatibility instead of DOT_MATCHES_ALL)
    result = result.replace(Regex("/\\*[\\s\\S]*?\\*/"), "")
    return result
}

/**
 * Fix quote issues.
 *
 * Converts single quotes to double quotes in property names.
 * Example: {'name': 'value'} -> {"name": 'value'}
 *
 * Note: This is a simplified implementation that only handles property names.
 * It may not handle all edge cases (e.g., quotes inside string values).
 *
 * @param json The JSON string
 * @return JSON with fixed quotes
 */
private fun fixQuotes(json: String): String {
    // Replace single quotes in property names: 'key': -> "key":
    return json.replace(Regex("'([^']*?)':"), "\"$1\":")
}

/**
 * Remove trailing commas.
 *
 * Handles:
 * - Trailing commas in objects: {a: 1,} -> {a: 1}
 * - Trailing commas in arrays: [1, 2,] -> [1, 2]
 *
 * @param json The JSON string
 * @return JSON without trailing commas
 */
private fun removeTrailingCommas(json: String): String {
    var result = json
    // Remove trailing commas before }
    result = result.replace(Regex(",\\s*}"), "}")
    // Remove trailing commas before ]
    result = result.replace(Regex(",\\s*]"), "]")
    return result
}

/**
 * Compact whitespace in JSON.
 *
 * Removes unnecessary whitespace while preserving string content.
 *
 * @param json The JSON string
 * @return JSON with compacted whitespace
 */
private fun compactWhitespace(json: String): String {
    val result = StringBuilder()
    var inString = false
    var escapeNext = false

    for (char in json) {
        when {
            escapeNext -> {
                escapeNext = false
                result.append(char)
            }
            char == '\\' && inString -> {
                escapeNext = true
                result.append(char)
            }
            char == '"' -> {
                inString = !inString
                result.append(char)
            }
            inString -> {
                result.append(char)
            }
            char.isWhitespace() -> {
                // Skip whitespace outside strings
            }
            else -> {
                result.append(char)
            }
        }
    }

    return result.toString()
}

/**
 * Stage 3: Repair JSON structure.
 *
 * Currently only handles bracket balancing.
 *
 * @param json The JSON string
 * @return JSON with repaired structure
 */
private fun repairJsonStructure(json: String): String = balanceBrackets(json)

/**
 * Balance brackets by adding missing closing brackets.
 *
 * Uses a stack-based algorithm to track unclosed brackets and
 * appends the missing closing brackets at the end.
 *
 * Example: {"name": "John", "items": [1, 2 -> {"name": "John", "items": [1, 2]}
 *
 * @param json The JSON string
 * @return JSON with balanced brackets
 */
private fun balanceBrackets(json: String): String {
    val stack = mutableListOf<Char>()
    val pairs = mapOf('{' to '}', '[' to ']')
    val closingToOpening = mapOf('}' to '{', ']' to '[')
    var inString = false
    var escapeNext = false
    val result = StringBuilder()

    // Track unclosed brackets and remove mismatched closing brackets
    for (char in json) {
        when {
            escapeNext -> {
                escapeNext = false
                result.append(char)
            }
            char == '\\' && inString -> {
                escapeNext = true
                result.append(char)
            }
            char == '"' -> {
                inString = !inString
                result.append(char)
            }
            !inString -> {
                when (char) {
                    '{', '[' -> {
                        stack.add(char)
                        result.append(char)
                    }
                    '}', ']' -> {
                        val expected = closingToOpening[char]
                        if (stack.lastOrNull() == expected) {
                            stack.removeLastOrNull()
                            result.append(char)
                        }
                        // Ignore mismatched closing brackets
                    }
                    else -> result.append(char)
                }
            }
            else -> result.append(char)
        }
    }

    // Append missing closing brackets
    val missing = stack.reversed().mapNotNull { pairs[it] }.joinToString("")
    return result.toString() + missing
}
