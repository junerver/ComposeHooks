package xyz.junerver.compose.hooks.userequest.utils

import java.io.Serializable
import xyz.junerver.compose.hooks.TParams

/**
 * Description:
 * @author Junerver
 * date: 2024/2/23-8:46
 * Email: junerver@gmail.com
 * Version: v1.0
 */
data class CachedData<TData>(
    val data: TData,
    val params: TParams,
    val time: Long,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CachedData<*>

        if (data != other.data) return false
        if (!params.contentEquals(other.params)) return false
        return time == other.time
    }

    override fun hashCode(): Int {
        var result = data?.hashCode() ?: 0
        result = 31 * result + params.contentHashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}
