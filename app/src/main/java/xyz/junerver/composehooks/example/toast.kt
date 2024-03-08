package xyz.junerver.composehooks.example

import android.content.Context
import android.widget.Toast

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-11:43
 * Email: junerver@gmail.com
 * Version: v1.0
 */
fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
