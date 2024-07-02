@file:Suppress("unused")

package xyz.junerver.compose.hooks.utils

import xyz.junerver.compose.hooks.DependencyList

/*
  Description:
  Author: Junerver
  Date: 2024/2/7-14:15
  Email: junerver@gmail.com
  Version: v1.0
*/

internal fun depsAreSame(oldDeps: DependencyList, deps: DependencyList): Boolean {
    // 如果数组长度不同，说明不相同
    if (oldDeps.size != deps.size) {
        return false
    }

    // 逐个比较数组中的元素是否相同
    for (i in oldDeps.indices) {
        val oldDep = oldDeps[i]
        val dep = deps[i]

        // 使用 equals 比较元素是否相同
        if (oldDep != dep) {
            return false
        }
    }

    // 如果所有元素都相同，则依赖项列表相同
    return true
}
