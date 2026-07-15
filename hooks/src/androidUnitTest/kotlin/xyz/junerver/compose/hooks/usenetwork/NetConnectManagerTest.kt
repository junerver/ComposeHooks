package xyz.junerver.compose.hooks.usenetwork

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
  Description: Android network state and listener ownership unit tests.
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class NetConnectManagerTest {
    @Test
    fun unvalidatedNetworkClearsStaleConnection() {
        assertEquals(
            NetworkState(),
            createNetworkState(isValidated = false, isCellular = false, isWifi = true),
        )
    }

    @Test
    fun validatedNetworkReportsTransport() {
        assertEquals(
            NetworkState(isConnect = true, connectType = ConnectType.Wifi),
            createNetworkState(isValidated = true, isCellular = false, isWifi = true),
        )
        assertEquals(
            NetworkState(isConnect = true, connectType = ConnectType.Mobile),
            createNetworkState(isValidated = true, isCellular = true, isWifi = false),
        )
    }

    @Test
    fun listenerRegistrySignalsOnlyFirstAndLastOwner() {
        val registry = NetworkListenerRegistry()
        val first: NetworkStateListener = {}
        val second: NetworkStateListener = {}

        assertTrue(registry.add(first))
        assertFalse(registry.add(first))
        assertFalse(registry.add(second))
        assertFalse(registry.remove(first))
        assertFalse(registry.remove(first))
        assertTrue(registry.remove(second))
    }

    @Test
    fun listenerSnapshotAllowsRemovalDuringDispatch() {
        val registry = NetworkListenerRegistry()
        val calls = mutableListOf<String>()
        lateinit var removingListener: NetworkStateListener
        removingListener = {
            calls += "removing"
            registry.remove(removingListener)
        }
        val remainingListener: NetworkStateListener = { calls += "remaining" }
        registry.add(removingListener)
        registry.add(remainingListener)

        registry.snapshot().forEach { it(NetworkState()) }

        assertEquals(listOf("removing", "remaining"), calls)
        assertEquals(listOf(remainingListener), registry.snapshot())
    }
}
