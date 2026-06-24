package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useboolean.BooleanHolder as BooleanHolderImpl
import xyz.junerver.compose.hooks.useboolean.useBooleanImpl

/**
 * A hook to conveniently manage Boolean state.
 *
 * This is a thin re-export facade: the implementation lives in the
 * [useboolean] subpackage as [useBooleanImpl]. It is kept in the root
 * package so consumers can continue to `import xyz.junerver.compose.hooks.useBoolean`
 * while the implementation can grow alongside its own helpers and types in
 * the subpackage. The Compose-style alias [rememberBoolean] (in `Hooks.kt`)
 * forwards through this function.
 *
 * @param default The default boolean value for the state. Default is `false`.
 * @return A [BooleanHolder] object.
 */
@Composable
fun useBoolean(default: Boolean = false): BooleanHolder = useBooleanImpl(default)

/** Re-exported type alias so existing `BooleanHolder` usages resolve unchanged. */
typealias BooleanHolder = BooleanHolderImpl
