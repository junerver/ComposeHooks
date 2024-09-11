package xyz.junerver.composehooks.utils

/*
  Description:
  Author: Junerver
  Date: 2024/4/11-10:32
  Email: junerver@gmail.com
  Version: v1.0
*/

fun String.subStringIf(length: Int = 100) =
    if (this.length > length) this.substring(0..length) else this
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
