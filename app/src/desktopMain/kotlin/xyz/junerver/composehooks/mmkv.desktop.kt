package xyz.junerver.composehooks

import ca.gosyer.appdirs.AppDirs
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import xyz.junerver.compose.hooks.notifyDefaultPersistentObserver

/*
  Description: 这段代码完全借鉴自[FunnySaltyFish](https://github.com/FunnySaltyFish)/[Transtation-KMP](https://github.com/FunnySaltyFish/Transtation-KMP)
  Author: pavi2410
  Date: 2024/9/11-15:11
  Email: junerver@gmail.com
  Version: v1.0
*/
class DataSaverProperties(private val filePath: String, private val encryptionKey: String) {
    private val properties = Properties()
    private val hashedKey = hashKey(encryptionKey)

    init {
        try {
            val f = File(filePath)
            if (!f.exists()) {
                f.parentFile.mkdirs()
                f.createNewFile()
            }
            FileReader(filePath).use { reader ->
                val decryptedReader = BufferedReader(
                    InputStreamReader(
                        CipherInputStream(
                            FileInputStream(filePath),
                            createCipher(Cipher.DECRYPT_MODE)
                        )
                    )
                )
                properties.load(decryptedReader)
            }
        } catch (e: FileNotFoundException) {
            // Handle file not found exception
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
        }
    }

    private fun saveProperties() {
        try {
            val encryptedWriter = BufferedWriter(
                OutputStreamWriter(
                    CipherOutputStream(
                        FileOutputStream(filePath),
                        createCipher(Cipher.ENCRYPT_MODE)
                    )
                )
            )
            properties.store(encryptedWriter, null)
        } catch (e: Exception) {
            // Handle file write exception
            e.printStackTrace()
        }
    }

    private fun createCipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val ivParameterSpec = IvParameterSpec(hashedKey.copyOfRange(0, 16))
        cipher.init(mode, keySpec, ivParameterSpec)
        return cipher
    }

    private fun hashKey(key: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(key.toByteArray())
    }

    fun <T> saveData(key: String, data: T) {
        properties[key] = data.toString()
        saveProperties()
    }

    fun <T> readData(key: String, default: T): T {
        val value = properties.getProperty(key) ?: return default
        return when (default) {
            is Int -> value.toIntOrNull() ?: default
            is Long -> value.toLongOrNull() ?: default
            is Boolean -> value.toBooleanStrictOrNull() ?: default
            is Double -> value.toDoubleOrNull() ?: default
            is Float -> value.toFloatOrNull() ?: default
            is String -> value
            else -> error("wrong type of default value！")
        } as T
    }

    fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }
}

val mmkv by lazy {
    // 读取 dotenv
    DataSaverProperties(
        filePath = CacheManager.baseDir.resolve("data_saver.properties").absolutePath,
        encryptionKey = "OpenSourceMagicKey"
    )
}

object CacheManager {
    private val appName = "Transtation"
    private val author = "FunnySaltyFish"
    private val appDir = AppDirs(appName, author)
    private val userHome = appDir.getUserDataDir()
    val baseDir = File(userHome)

    var cacheDir: File = baseDir.resolve("cache")
}

actual fun mmkvSave(key: String, value: Any?) {
    when (value) {
        is Int -> mmkv.saveData(key, value)
        is Long -> mmkv.saveData(key, value)
        is Double -> mmkv.saveData(key, value)
        is Float -> mmkv.saveData(key, value)
        is Boolean -> mmkv.saveData(key, value)
        is String -> mmkv.saveData(key, value)
        is ByteArray -> mmkv.saveData(key, value)
    }
    notifyDefaultPersistentObserver(key)
}

actual fun mmkvGet(key: String, value: Any): Any {
    return when (value) {
        is Int -> mmkv.readData(key, value)
        is Long -> mmkv.readData(key, value)
        is Double -> mmkv.readData(key, value)
        is Float -> mmkv.readData(key, value)
        is Boolean -> mmkv.readData(key, value)
        is String -> mmkv.readData(key, value)
        is ByteArray -> mmkv.readData(key, value)
        else -> error("wrong type of default value！")
    }
}

actual fun mmkvClear(key: String) {
    mmkv.remove(key)
}
