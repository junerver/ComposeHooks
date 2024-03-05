package xyz.junerver.compose.hooks.usenetwork

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 * @author Junerver
 * date: 2024/2/6-16:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
data class NetworkState(
    val isConnect: Boolean,
    val connectType: ConnectType
)

/**
 * 直接到处使用该hook会导致大量的监听器回调创建，虽影响不大，但是更建议使用
 * [NetworkProvider]作为根组件，配合 [rememberNetwork] 来使用。
 */
@Composable
fun useNetwork(): NetworkState {
    NetConnectManager.init(LocalContext.current)
    val (network, setNetwork) = useState(
        NetworkState(
            isConnect = NetConnectManager.isConnected(),
            connectType = NetConnectManager.getConnectType()
        )
    )
    DisposableEffect(Unit) {
        val typeChangeListener: (type: ConnectType) -> Unit = {
            setNetwork(network.copy(connectType = it))
        }
        val statusChangeListener: (isAvailable: Boolean) -> Unit = {
            setNetwork(network.copy(isConnect = it))
        }

        NetConnectManager.addListener(typeChangeListener, statusChangeListener)
        onDispose {
            NetConnectManager.removeListener(typeChangeListener, statusChangeListener)
        }
    }
    return network
}

/**
 * 使用提供器的好处是在整个根组件的范围内，都可以使用[rememberNetwork]获得同一个状态
 */
@Composable
fun NetworkProvider(content: @Composable () -> Unit) {
    val network = useNetwork()
    CompositionLocalProvider(LocalNetwork provides network) {
        content()
    }
}

private val LocalNetwork: ProvidableCompositionLocal<NetworkState> =
    staticCompositionLocalOf { error("provide first") }

/**
 * 务必注意：必须配合[NetworkProvider]在子组件内使用。
 */
@Composable
fun rememberNetwork() = LocalNetwork.current
