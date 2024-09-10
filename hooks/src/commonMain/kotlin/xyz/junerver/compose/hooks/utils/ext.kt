package xyz.junerver.compose.hooks.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-10:44
  Email: junerver@gmail.com
  Version: v1.0
*/

internal val currentTime: Instant
    get() = Clock.System.now()
