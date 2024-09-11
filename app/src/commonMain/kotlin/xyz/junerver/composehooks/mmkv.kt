package xyz.junerver.composehooks

// import com.ctrip.flight.mmkv.defaultMMKV

/*
  Description:
  Author: Junerver
  Date: 2024/9/11-11:02
  Email: junerver@gmail.com
  Version: v1.0
*/

expect fun mmkvSave(key: String, value: Any?)

expect fun mmkvGet(key: String, value: Any): Any

expect fun mmkvClear(key: String)
