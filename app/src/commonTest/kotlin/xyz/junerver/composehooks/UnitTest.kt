package xyz.junerver.composehooks

import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charsets
import kotlin.test.Test

class UnitTest {
    @Test
    fun test() {
        println("----------------------------------------------")
        println(ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
        println("----------------------------------------------")
    }
}
