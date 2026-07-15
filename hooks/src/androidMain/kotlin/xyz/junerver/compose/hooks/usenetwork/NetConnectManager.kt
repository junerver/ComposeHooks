package xyz.junerver.compose.hooks.usenetwork

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService

/*
  Description:原作者：[青杉](https://juejin.cn/user/3175045310722119/posts)，
  文章博客：[Android 监听网络状态变化（无切换中间态版）](https://juejin.cn/post/7324345717908717587)
  Author: 青杉
  Date: 2024/2/6-16:37
  Version: v1.0
*/
@SuppressLint("WrongCommentType")
sealed class ConnectType(val value: Int) {
    data object Mobile : ConnectType(0)

    data object Wifi : ConnectType(1)

    data object None : ConnectType(-1)

    companion object {
        fun convert2Type(value: Int): ConnectType = when (value) {
            Mobile.value -> Mobile
            Wifi.value -> Wifi
            else -> None
        }
    }
}

internal fun createNetworkState(isValidated: Boolean, isCellular: Boolean, isWifi: Boolean): NetworkState {
    if (!isValidated) return NetworkState()
    val connectType = when {
        isCellular -> ConnectType.Mobile
        isWifi -> ConnectType.Wifi
        else -> ConnectType.None
    }
    return NetworkState(isConnect = true, connectType = connectType)
}

internal class NetworkListenerRegistry {
    private val listeners = linkedSetOf<NetworkStateListener>()

    /** Returns true when this listener becomes the first active owner. */
    fun add(listener: NetworkStateListener): Boolean {
        if (!listeners.add(listener)) return false
        return listeners.size == 1
    }

    /** Returns true when this removal releases the final active owner. */
    fun remove(listener: NetworkStateListener): Boolean {
        if (!listeners.remove(listener)) return false
        return listeners.isEmpty()
    }

    fun snapshot(): List<NetworkStateListener> = listeners.toList()
}

internal object NetConnectManager {
    private const val NETWORK_LOSS_DELAY_MILLIS = 500L

    private val lock = Any()
    private val listeners = NetworkListenerRegistry()
    private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Handler(Looper.getMainLooper()) }

    private var applicationContext: Context? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkReceiver: BroadcastReceiver? = null
    private var pendingRefresh: Runnable? = null
    private var currentState = NetworkState()

    fun currentState(context: Context): NetworkState {
        val manager = try {
            context.getSystemService<ConnectivityManager>()
        } catch (_: SecurityException) {
            null
        }
        return manager?.readNetworkState() ?: NetworkState()
    }

    fun addListener(context: Context, listener: NetworkStateListener): NetworkState = synchronized(lock) {
        if (listeners.add(listener)) {
            startMonitoringLocked(context.applicationContext ?: context)
        }
        currentState
    }

    fun removeListener(listener: NetworkStateListener) {
        synchronized(lock) {
            if (listeners.remove(listener)) {
                stopMonitoringLocked()
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt", "UnspecifiedRegisterReceiverFlag")
    private fun startMonitoringLocked(context: Context) {
        applicationContext = context
        val manager = try {
            context.getSystemService<ConnectivityManager>()
        } catch (_: SecurityException) {
            null
        }
        connectivityManager = manager
        currentState = manager?.readNetworkState() ?: NetworkState()
        if (manager == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val callback = createNetworkCallback()
            networkCallback = callback
            try {
                manager.registerDefaultNetworkCallback(callback)
            } catch (_: SecurityException) {
                networkCallback = null
            }
        } else {
            val receiver = createNetworkReceiver()
            networkReceiver = receiver
            try {
                context.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            } catch (_: SecurityException) {
                networkReceiver = null
            }
        }
    }

    private fun stopMonitoringLocked() {
        pendingRefresh?.let { mainHandler.removeCallbacks(it) }
        pendingRefresh = null

        val manager = connectivityManager
        val callback = networkCallback
        networkCallback = null
        if (manager != null && callback != null) {
            try {
                manager.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // The callback was already detached by the platform.
            }
        }

        val context = applicationContext
        val receiver = networkReceiver
        networkReceiver = null
        if (context != null && receiver != null) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                // The receiver was already detached by the platform.
            }
        }

        connectivityManager = null
        applicationContext = null
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            scheduleNetworkRefresh(this, NETWORK_LOSS_DELAY_MILLIS)
        }

        override fun onUnavailable() {
            scheduleNetworkRefresh(this, NETWORK_LOSS_DELAY_MILLIS)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val nextState = networkCapabilities.toNetworkState()
            mainHandler.post {
                val stateListeners = synchronized(lock) {
                    if (networkCallback !== this) return@post
                    cancelPendingRefreshLocked()
                    updateStateLocked(nextState)
                }
                stateListeners.forEach { it(nextState) }
            }
        }
    }

    private fun createNetworkReceiver(): BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mainHandler.post {
                val nextState: NetworkState
                val stateListeners = synchronized(lock) {
                    if (networkReceiver !== this) return@post
                    nextState = connectivityManager?.readNetworkState() ?: NetworkState()
                    updateStateLocked(nextState)
                }
                stateListeners.forEach { it(nextState) }
            }
        }
    }

    private fun scheduleNetworkRefresh(callback: ConnectivityManager.NetworkCallback, delayMillis: Long) {
        mainHandler.post {
            synchronized(lock) {
                if (networkCallback !== callback) return@post
                cancelPendingRefreshLocked()
                val refresh = Runnable {
                    val nextState: NetworkState
                    val stateListeners = synchronized(lock) {
                        if (networkCallback !== callback) return@Runnable
                        pendingRefresh = null
                        nextState = connectivityManager?.readNetworkState() ?: NetworkState()
                        updateStateLocked(nextState)
                    }
                    stateListeners.forEach { it(nextState) }
                }
                pendingRefresh = refresh
                mainHandler.postDelayed(refresh, delayMillis)
            }
        }
    }

    private fun cancelPendingRefreshLocked() {
        pendingRefresh?.let { mainHandler.removeCallbacks(it) }
        pendingRefresh = null
    }

    private fun updateStateLocked(nextState: NetworkState): List<NetworkStateListener> {
        if (currentState == nextState) return emptyList()
        currentState = nextState
        return listeners.snapshot()
    }

    private fun ConnectivityManager.readNetworkState(): NetworkState = try {
        val capabilities = activeNetwork?.let(::getNetworkCapabilities)
        capabilities?.toNetworkState() ?: NetworkState()
    } catch (_: SecurityException) {
        NetworkState()
    }

    private fun NetworkCapabilities.toNetworkState(): NetworkState = createNetworkState(
        isValidated = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
        isCellular = hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        isWifi = hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
    )
}

internal typealias NetworkStateListener = (state: NetworkState) -> Unit
