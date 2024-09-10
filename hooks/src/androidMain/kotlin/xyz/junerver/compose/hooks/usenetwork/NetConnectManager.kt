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
        fun convert2Type(value: Int): ConnectType {
            return when (value) {
                Mobile.value -> Mobile
                Wifi.value -> Wifi
                else -> None
            }
        }
    }
}

internal object NetConnectManager {

    private var mConnectivityManager: ConnectivityManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mNetTypeListener = mutableListOf<NetTypeChangeListener>()
    private val mNetStateListener = mutableListOf<NetStatusChangeListener>()
    private var mCurrentConnectType: ConnectType? = null
    private var mIsNetAvailable: Boolean? = null

    /**
     * 初始化
     */
    @SuppressLint("ObsoleteSdkInt")
    fun init(context: Context): NetConnectManager {
        mConnectivityManager = context.getSystemService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mConnectivityManager?.registerDefaultNetworkCallback(DefaultNetConnectCallback())
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(
                NetConnectReceiver(),
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        return this
    }

    /**
     * 两个监听一起注册
     */
    fun addListener(
        typeChangeListener: NetTypeChangeListener,
        statusChangeListener: NetStatusChangeListener,
    ) {
        addNetTypeChangeListener(typeChangeListener)
        addNetStatusChangeListener(statusChangeListener)
    }

    /**
     * 两个监听一起移除
     */
    fun removeListener(
        typeChangeListener: NetTypeChangeListener,
        statusChangeListener: NetStatusChangeListener,
    ) {
        removeNetTypeChangeListener(typeChangeListener)
        removeNetStatusChangeListener(statusChangeListener)
    }

    /**
     * 注册网络类型监听
     */
    private fun addNetTypeChangeListener(listener: NetTypeChangeListener) {
        mNetTypeListener.add(listener)
    }

    /**
     * 反注册网络类型监听
     */

    private fun removeNetTypeChangeListener(listener: NetTypeChangeListener) {
        mNetTypeListener.remove(listener)
    }

    /**
     * 注册网络状态监听
     */
    private fun addNetStatusChangeListener(listener: NetStatusChangeListener) {
        mNetStateListener.add(listener)
    }

    /**
     * 反注册网络状态监听
     */
    private fun removeNetStatusChangeListener(listener: NetStatusChangeListener) {
        mNetStateListener.remove(listener)
    }

    /**
     * 获取当前网络类型
     */
    fun getConnectType(): ConnectType {
        if (mConnectivityManager == null) {
            throw UninitializedPropertyAccessException("请先调用init()初始化")
        }
        return mCurrentConnectType ?: mConnectivityManager?.getNetworkCapabilities(
            mConnectivityManager?.activeNetwork
        ).let {
            return if (it?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                ConnectType.Mobile
            } else if (it?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                ConnectType.Wifi
            } else {
                ConnectType.None
            }
        }
    }

    /**
     * 获取当前是否网络已连接
     */
    fun isConnected(): Boolean {
        if (mConnectivityManager == null) {
            throw UninitializedPropertyAccessException("请先调用init()初始化")
        }
        return (
            mIsNetAvailable
                ?: mConnectivityManager?.getNetworkCapabilities(mConnectivityManager?.activeNetwork)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ) == true
    }

    private class DefaultNetConnectCallback : ConnectivityManager.NetworkCallback() {

        override fun onLost(network: Network) {
            super.onLost(network)
            mCurrentConnectType = ConnectType.None
            mainHandler.postDelayed({
                if (mCurrentConnectType == ConnectType.None && mIsNetAvailable == true) {
                    mIsNetAvailable = false
                    mNetStateListener.forEach { it.invoke(false) }
                    mNetTypeListener.forEach { it(ConnectType.None) }
                }
            }, 500)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            mainHandler.post {
                val isConnected =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val isCellular =
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                val isWifi =
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

                if (isConnected) {
                    val newConnectType =
                        if (isCellular) ConnectType.Mobile else if (isWifi) ConnectType.Wifi else ConnectType.None
                    if (mIsNetAvailable == null || mIsNetAvailable == false) {
                        mIsNetAvailable = true
                        mNetStateListener.forEach { it(true) }
                    }
                    if (mCurrentConnectType != newConnectType) {
                        mCurrentConnectType = newConnectType
                        mNetTypeListener.forEach { it(newConnectType) }
                    }
                }
            }
        }
    }

    private class NetConnectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            @Suppress("DEPRECATION")
            val activityNetworkInfo =
                context?.getSystemService(ConnectivityManager::class.java)?.allNetworkInfo?.firstOrNull {
                    (it.type == ConnectType.Mobile.value || it.type == ConnectType.Wifi.value) && it.isConnected
                }
            if (activityNetworkInfo != null) {
                if (mIsNetAvailable == null || mIsNetAvailable == false) {
                    mIsNetAvailable = true
                    mNetStateListener.forEach { it(true) }
                }
                @Suppress("DEPRECATION")
                ConnectType.convert2Type(activityNetworkInfo.type).let { connectType ->
                    if (connectType != mCurrentConnectType) {
                        mCurrentConnectType = connectType
                        mNetTypeListener.forEach { it(connectType) }
                    }
                }
                return
            }
            mCurrentConnectType = ConnectType.None
            mainHandler.postDelayed({
                if (mCurrentConnectType == ConnectType.None && mIsNetAvailable == true) {
                    mIsNetAvailable = false
                    mNetStateListener.forEach { it(false) }
                    mNetTypeListener.forEach { it(ConnectType.None) }
                }
            }, 500)
        }
    }
}

internal typealias NetTypeChangeListener = (type: ConnectType) -> Unit
internal typealias NetStatusChangeListener = (isAvailable: Boolean) -> Unit
