package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.compose.hooks.utils.CacheManager

/*
  Description: CacheManager contract tests
  Author: Junerver
  Date: 2026/7/2
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseCachePluginTest {
    private fun uniqueKey(prefix: String): String = "$prefix-${kotlin.random.Random.nextInt()}"

    @Test
    fun save_cache_stores_value_and_params() {
        val key = uniqueKey("save")
        val data = CachedData("test_data", mapOf("page" to 1))

        val saved = CacheManager.saveCache(key, 10.seconds, data)
        val cached = CacheManager.getCache<String>(key)

        assertTrue(saved)
        assertNotNull(cached)
        assertEquals("test_data", cached.data)
        assertEquals(mapOf("page" to 1), cached.params)
    }

    @Test
    fun get_cache_returns_null_for_missing_key() {
        assertNull(CacheManager.getCache<String>(uniqueKey("missing")))
    }

    @Test
    fun zero_duration_does_not_store_cache() {
        val key = uniqueKey("zero-duration")

        val saved = CacheManager.saveCache(key, Duration.ZERO, CachedData("value"))

        assertFalse(saved)
        assertNull(CacheManager.getCache<String>(key))
    }

    @Test
    fun negative_one_duration_keeps_cache_available() {
        val key = uniqueKey("permanent")

        val saved = CacheManager.saveCache(key, (-1).seconds, CachedData("value"))

        assertTrue(saved)
        assertEquals("value", CacheManager.getCache<String>(key)?.data)
    }

    @Test
    fun expired_cache_is_removed_when_read() {
        val key = uniqueKey("expire")
        CacheManager.saveCache(key, 10.milliseconds, CachedData("value"))

        Thread.sleep(20)

        assertNull(CacheManager.getCache<String>(key))
        assertNull(CacheManager.getCache<String>(key))
    }

    @Test
    fun save_cache_overwrites_previous_entry_for_same_key() {
        val key = uniqueKey("overwrite")

        CacheManager.saveCache(key, 10.seconds, CachedData("first", "p1"))
        CacheManager.saveCache(key, 10.seconds, CachedData("second", "p2"))

        val cached = CacheManager.getCache<String>(key)
        assertNotNull(cached)
        assertEquals("second", cached.data)
        assertEquals("p2", cached.params)
    }

    @Test
    fun clear_cache_removes_requested_entries_only() {
        val removedKey = uniqueKey("removed")
        val keptKey = uniqueKey("kept")

        CacheManager.saveCache(removedKey, "removed")
        CacheManager.saveCache(keptKey, "kept")
        CacheManager.clearCache(removedKey)

        assertEquals("fallback", CacheManager.getCache(removedKey, "fallback"))
        assertEquals("kept", CacheManager.getCache(keptKey, "fallback"))
    }

    @Test
    fun clear_cache_supports_multiple_keys() {
        val keys = List(3) { index -> uniqueKey("multi-$index") }
        keys.forEach { CacheManager.saveCache(it, "value") }

        CacheManager.clearCache(*keys.take(2).toTypedArray())

        assertEquals("fallback", CacheManager.getCache(keys[0], "fallback"))
        assertEquals("fallback", CacheManager.getCache(keys[1], "fallback"))
        assertEquals("value", CacheManager.getCache(keys[2], "fallback"))
    }

    @Test
    fun cached_data_equality_includes_creation_time() {
        val first = CachedData("same", "params")
        Thread.sleep(2)
        val second = CachedData("same", "params")

        assertNotEquals(first, second)
        assertEquals(first.data, second.data)
        assertEquals(first.params, second.params)
    }

    @Test
    fun get_cache_with_default_returns_default_value_when_missing() {
        assertEquals("default", CacheManager.getCache(uniqueKey("default"), "default"))
    }

    @Test
    fun cache_supports_multiple_value_types() {
        val stringKey = uniqueKey("string")
        val intKey = uniqueKey("int")
        val listKey = uniqueKey("list")

        CacheManager.saveCache(stringKey, "string_value")
        CacheManager.saveCache(intKey, 42)
        CacheManager.saveCache(listKey, listOf(1, 2, 3))

        assertEquals("string_value", CacheManager.getCache(stringKey, ""))
        assertEquals(42, CacheManager.getCache(intKey, 0))
        assertEquals(listOf(1, 2, 3), CacheManager.getCache(listKey, emptyList<Int>()))
    }

    @Test
    fun save_cache_preserves_null_params() {
        val key = uniqueKey("null-params")
        CacheManager.saveCache(key, 10.seconds, CachedData("value", null))

        val cached = CacheManager.getCache<String>(key)
        assertNotNull(cached)
        assertNull(cached.params)
    }

    @Test
    fun bulk_writes_keep_each_entry_accessible() {
        val keys = (0 until 20).map { index ->
            val key = uniqueKey("bulk-$index")
            CacheManager.saveCache(key, 10.seconds, CachedData("value_$index"))
            key
        }

        keys.forEachIndexed { index, key ->
            assertEquals("value_$index", CacheManager.getCache<String>(key)?.data)
        }
    }

    @Test
    fun concurrent_writes_keep_each_entry_accessible() = runTest {
        val keys = List(100) { index -> uniqueKey("concurrent-$index") }

        val saved = keys.mapIndexed { index, key ->
            async(Dispatchers.Default) {
                CacheManager.saveCache(key, 10.seconds, CachedData("value_$index"))
            }
        }.awaitAll()

        assertTrue(saved.all { it })
        keys.forEachIndexed { index, key ->
            assertEquals("value_$index", CacheManager.getCache<String>(key)?.data)
        }
    }
}
