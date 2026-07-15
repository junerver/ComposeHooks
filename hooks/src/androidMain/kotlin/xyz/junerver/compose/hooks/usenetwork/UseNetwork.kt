@file:Suppress("ktlint:standard:function-naming")

package xyz.junerver.compose.hooks.usenetwork

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.ComposeComponent
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.usecontext.ReactContext
import xyz.junerver.compose.hooks.usecontext.createContextImpl

/*
  Description:
  Author: Junerver
  Date: 2024/2/6-16:38
  Email: junerver@gmail.com
  Version: v1.0
*/

@Stable
data class NetworkState(
    val isConnect: Boolean = false,
    val connectType: ConnectType = ConnectType.None,
)

internal val NetworkContext: ReactContext<State<NetworkState>> by lazy { createContextImpl(mutableStateOf(NetworkState())) }

/**
 * 直接到处使用该hook会导致大量的监听器回调创建，虽影响不大，但是更建议使用
 * [NetworkProvider]作为根组件，配合 [xyz.junerver.compose.hooks.rememberNetwork] 来使用。
 */
@Composable
fun useNetwork(): State<NetworkState> {
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    val initialState = useCreation(context) { NetConnectManager.currentState(context) }.current
    return produceState(
        initialValue = initialState,
        context,
    ) {
        val listener: NetworkStateListener = { value = it }
        value = NetConnectManager.addListener(context, listener)
        awaitDispose {
            NetConnectManager.removeListener(listener)
        }
    }
}

/**
 * 使用提供器的好处是在整个根组件的范围内，都可以使用[xyz.junerver.compose.hooks.rememberNetwork]获得同一个状态
 */
@Composable
fun NetworkProvider(content: ComposeComponent) {
    val network = useNetwork()
    NetworkContext.Provider(value = network) {
        content()
    }
}
