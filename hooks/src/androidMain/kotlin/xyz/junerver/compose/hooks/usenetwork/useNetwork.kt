package xyz.junerver.compose.hooks.usenetwork

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.ComposeComponent
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useContext

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

val NetworkContext = createContext(NetworkState())

/**
 * 直接到处使用该hook会导致大量的监听器回调创建，虽影响不大，但是更建议使用
 * [NetworkProvider]作为根组件，配合 [rememberNetwork] 来使用。
 */
@Composable
fun useNetwork(): NetworkState {
    val context = LocalContext.current
    remember {
        NetConnectManager.init(context)
    }
    return produceState(
        initialValue = NetworkState(
            isConnect = NetConnectManager.isConnected(),
            connectType = NetConnectManager.getConnectType()
        )
    ) {
        val typeChangeListener: (type: ConnectType) -> Unit = {
            value = value.copy(connectType = it)
        }
        val statusChangeListener: (isAvailable: Boolean) -> Unit = {
            value = value.copy(isConnect = it)
        }
        NetConnectManager.addListener(typeChangeListener, statusChangeListener)
        awaitDispose {
            NetConnectManager.removeListener(typeChangeListener, statusChangeListener)
        }
    }.value
}

/**
 * 使用提供器的好处是在整个根组件的范围内，都可以使用[rememberNetwork]获得同一个状态
 */
@Composable
fun NetworkProvider(content: ComposeComponent) {
    val network = useNetwork()
    NetworkContext.Provider(value = network) {
        content()
    }
}

/**
 * 务必注意：必须配合[NetworkProvider]在子组件内使用。
 */
@Composable
fun rememberNetwork() = useContext(context = NetworkContext)
