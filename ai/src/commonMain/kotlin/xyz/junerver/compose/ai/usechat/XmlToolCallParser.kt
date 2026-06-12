package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal val XML_TOOL_CALL_REGEX = Regex("""<\s*tool_call\s*>[\s\S]*?<\s*/\s*tool_call\s*>""")
private val XML_TOOL_CALL_CONTENT_REGEX = Regex("""<\s*tool_call\s*>([\s\S]*?)<\s*/\s*tool_call\s*>""")
private val XML_FUNCTION_NAME_REGEX = Regex("""<\s*function\s*=\s*([^>]+)\s*>""")
private val XML_PARAMETER_REGEX = Regex("""<\s*parameter\s*=\s*([^>]+)\s*>([\s\S]*?)<\s*/\s*parameter\s*>""")

internal fun parseXmlToolCalls(content: String): List<ToolCallPart> {
    val toolCalls = mutableListOf<ToolCallPart>()
    XML_TOOL_CALL_CONTENT_REGEX.findAll(content).forEachIndexed { index, match ->
        val toolCallContent = match.groupValues[1].trim()
        val functionName = XML_FUNCTION_NAME_REGEX.find(toolCallContent)?.groupValues?.get(1)?.trim()
            ?: return@forEachIndexed
        val args = buildJsonObject {
            XML_PARAMETER_REGEX.findAll(toolCallContent).forEach { paramMatch ->
                val paramName = paramMatch.groupValues[1].trim()
                val paramValue = paramMatch.groupValues[2].trim()
                if (paramValue.equals("null", ignoreCase = true) || paramValue.equals("None", ignoreCase = true)) {
                    // Skip null parameters
                } else {
                    put(paramName, JsonPrimitive(paramValue))
                }
            }
        }
        toolCalls.add(
            ToolCallPart(
                toolCallId = "xml_toolcall_$index",
                toolName = functionName,
                args = args,
            ),
        )
    }
    return toolCalls
}
