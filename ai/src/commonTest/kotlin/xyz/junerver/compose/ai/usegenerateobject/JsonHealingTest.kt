package xyz.junerver.compose.ai.usegenerateobject

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonHealingTest {
    @Test
    fun testExtractJsonFromMarkdown() {
        val input =
            """
            ```json
            {"name": "John"}
            ```
            """.trimIndent()
        val result = healJson(input)
        assertEquals("""{"name":"John"}""", result)
    }

    @Test
    fun testExtractJsonFromText() {
        val input = """Here's the result: {"name": "John", "age": 30} and that's it"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testRemoveSingleLineComments() {
        val input = """{"name": "John", // this is a comment
"age": 30}"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testRemoveMultiLineComments() {
        val input = """{"name": "John", /* this is a
multi-line comment */ "age": 30}"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testFixSingleQuotes() {
        val input = """{'name': 'John', 'age': 30}"""
        val result = healJson(input)
        assertEquals("""{"name":'John',"age":30}""", result)
    }

    @Test
    fun testRemoveTrailingCommasInObject() {
        val input = """{"name": "John", "age": 30,}"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testRemoveTrailingCommasInArray() {
        val input = """{"items": [1, 2, 3,]}"""
        val result = healJson(input)
        assertEquals("""{"items":[1,2,3]}""", result)
    }

    @Test
    fun testBalanceMissingClosingBrace() {
        val input = """{"name": "John", "age": 30"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testBalanceMissingClosingBracket() {
        val input = """{"items": [1, 2, 3}"""
        val result = healJson(input)
        assertEquals("""{"items":[1,2,3]}""", result)
    }

    @Test
    fun testBalanceNestedStructures() {
        val input = """{"user": {"name": "John", "items": [1, 2"""
        val result = healJson(input)
        assertEquals("""{"user":{"name":"John","items":[1,2]}}""", result)
    }

    @Test
    fun testComplexScenario() {
        val input =
            """
            ```json
            {
                'name': 'John', // user name
                'age': 30,
                'items': [1, 2, 3,],
                'address': {
                    'city': 'NYC'
            ```
            """.trimIndent()
        val result = healJson(input)
        assertEquals("""{"name":'John',"age":30,"items":[1,2,3],"address":{"city":'NYC'}}""", result)
    }

    @Test
    fun testDisableHealing() {
        val input = """{'name': 'John',}"""
        val result = healJson(input, enableHealing = false)
        assertEquals("""{'name': 'John'}""", result)
    }

    @Test
    fun testEmptyString() {
        val result = healJson("")
        assertEquals("", result)
    }

    @Test
    fun testValidJson() {
        val input = """{"name": "John", "age": 30}"""
        val result = healJson(input)
        assertEquals("""{"name":"John","age":30}""", result)
    }

    @Test
    fun testArrayAtRoot() {
        val input = """[1, 2, 3,]"""
        val result = healJson(input)
        assertEquals("""[1,2,3]""", result)
    }

    @Test
    fun testEscapedQuotesInString() {
        val input = """{"message": "He said \"hello\""}"""
        val result = healJson(input)
        assertEquals("""{"message":"He said \"hello\""}""", result)
    }
}
