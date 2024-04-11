package xyz.junerver.composehooks.utils

/**
 * Description:
 * @author Junerver
 * date: 2024/4/11-10:32
 * Email: junerver@gmail.com
 * Version: v1.0
 */
fun String.subStringIf(length: Int = 100) =
    if (this.length > length) this.substring(0..length) else this
