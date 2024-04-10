package xyz.junerver.composehooks

import android.app.Application
import com.tencent.mmkv.MMKV

/**
 * Description:
 * @author Junerver
 * date: 2024/4/10-16:00
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
