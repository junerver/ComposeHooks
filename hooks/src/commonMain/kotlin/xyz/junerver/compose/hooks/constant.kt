package xyz.junerver.compose.hooks

import xyz.junerver.compose.hooks.useform.FormInstance

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-9:58
  Email: junerver@gmail.com
  Version: v1.0
*/

internal const val KEY_PREFIX = "HOOK_INTERNAL_"

private const val CACHE_KEY_PREFIX = "${KEY_PREFIX}CACHE_MANAGER_"
internal val String.cacheKey: String
    get() = "${CACHE_KEY_PREFIX}$this"

private const val FORM_KEY_PREFIX = "${KEY_PREFIX}FORM_FIELD_"

internal fun String.genFormFieldKey(formInstance: FormInstance) = "${FORM_KEY_PREFIX}${formInstance}_$this"

private const val PERSISTENT_KEY_PREFIX = "${KEY_PREFIX}USE_PERSISTENT_"
internal val String.persistentKey: String
    get() = "${PERSISTENT_KEY_PREFIX}$this"
