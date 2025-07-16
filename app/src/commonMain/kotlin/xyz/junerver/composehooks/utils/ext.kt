package xyz.junerver.composehooks.utils

import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.Instant

/*
  Description:
  Author: Junerver
  Date: 2024/4/11-10:32
  Email: junerver@gmail.com
  Version: v1.0
*/

fun String.subStringIf(length: Int = 100) = if (this.length > length) this.substring(0..length) else this
//
// @Composable
// inline fun <reified VM : ViewModel> composeViewModel():VM{
//    val factory = object : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
//            return VM::class.constructors.first().call() as T
//        }
//    }
//    return viewModel(factory = factory)
// }

fun Double.formatToDecimalPlaces(places: Int): String {
    val multiplier = 10.0.pow(places)
    val rounded = (this * multiplier).toLong().toDouble() / multiplier
    return rounded.toString().let {
        val parts = it.split(".")
        if (parts.size == 1) {
            "$it.0".padEnd(it.length + places + 1, '0')
        } else {
            parts[0] + "." + parts[1].padEnd(places, '0')
        }
    }
}

fun now(): Instant = Clock.System.now()
