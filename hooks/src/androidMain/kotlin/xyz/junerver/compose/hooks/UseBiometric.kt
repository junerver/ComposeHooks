package xyz.junerver.compose.hooks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates
import xyz.junerver.compose.hooks.useDynamicOptions

/*
  Description: use biometrics conveniently
  Author: Junerver
  Date: 2024/7/19-16:15
  Email: junerver@gmail.com
  Version: v1.0
*/

@Stable
data class BiometricOptions internal constructor(
    var onAuthenticationError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
    var onAuthenticationFailed: () -> Unit = {},
    var onAuthenticationSucceeded: (result: BiometricPrompt.AuthenticationResult) -> Unit =
        { _ -> },
    var title: String = "Biometric",
    var negativeButtonText: String = "cancel",
) {
    companion object : Options<BiometricOptions>(::BiometricOptions)
}

@Composable
private fun useBiometric(options: BiometricOptions = remember { BiometricOptions() }): Pair<() -> Unit, State<Boolean>> {
    val (isAuthed, setIsAuthed) = _useGetState(default = false)
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        setIsAuthed(it.resultCode == Activity.RESULT_OK)
    }
    val open = useCreation(launcher, context, options) {
        {
            val intent = BiometricActivity.newIntent(context, options)
            launchBiometricRequest(BiometricActivity.requestIdFrom(intent)) {
                launcher.launch(intent)
            }
        }
    }.current
    return Pair(
        open,
        isAuthed,
    )
}

@Composable
fun useBiometric(optionsOf: BiometricOptions.() -> Unit = {}) = useBiometric(useBiometricOptions(optionsOf))

@Composable
internal fun useBiometricOptions(optionsOf: BiometricOptions.() -> Unit): BiometricOptions =
    useDynamicOptions(optionsOf, ::createBiometricOptions)

internal fun createBiometricOptions(optionsOf: BiometricOptions.() -> Unit): BiometricOptions =
    BiometricOptions.optionOf(optionsOf)

class BiometricActivity : FragmentActivity() {
    private var requestId: Int = BiometricRequestStore.MISSING_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestId = intent.getIntExtra(EXTRA_REQUEST_ID, BiometricRequestStore.MISSING_ID)
        val options = if (requestId == BiometricRequestStore.MISSING_ID) {
            legacyBiometricOptionsOrNull()
        } else {
            BiometricRequestStore.get(requestId)
        }
        if (options == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(options.title)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText(options.negativeButtonText)
            .build()
        createBiometricPrompt(this, options).authenticate(promptInfo)
    }

    override fun onDestroy() {
        releaseBiometricRequest(requestId, isChangingConfigurations)
        super.onDestroy()
    }

    companion object {
        internal const val EXTRA_REQUEST_ID = "xyz.junerver.compose.hooks.BIOMETRIC_REQUEST_ID"

        @Deprecated("Use newIntent() to pass request-scoped biometric options")
        var options: BiometricOptions by Delegates.notNull()

        @Suppress("DEPRECATION")
        fun newIntent(context: Context, options: BiometricOptions): Intent {
            this.options = options
            val requestId = BiometricRequestStore.put(options)
            return try {
                Intent(context, BiometricActivity::class.java).putExtra(EXTRA_REQUEST_ID, requestId)
            } catch (exception: Exception) {
                BiometricRequestStore.remove(requestId)
                throw exception
            }
        }

        internal fun requestIdFrom(intent: Intent): Int = intent.getIntExtra(EXTRA_REQUEST_ID, BiometricRequestStore.MISSING_ID)
    }
}

internal object BiometricRequestStore {
    const val MISSING_ID: Int = -1

    private val nextId = AtomicInteger(0)
    private val lock = Any()
    private val optionsById = mutableMapOf<Int, BiometricOptions>()

    fun put(options: BiometricOptions): Int = synchronized(lock) {
        val id = nextId.updateAndGet { previous ->
            if (previous == Int.MAX_VALUE) 0 else previous + 1
        }
        optionsById[id] = options.copy()
        id
    }

    fun get(id: Int): BiometricOptions? = synchronized(lock) {
        optionsById[id]
    }

    fun remove(id: Int): BiometricOptions? = synchronized(lock) {
        optionsById.remove(id)
    }

    fun clear() {
        synchronized(lock) {
            optionsById.clear()
        }
    }
}

@Suppress("DEPRECATION")
private fun legacyBiometricOptionsOrNull(): BiometricOptions? = try {
    BiometricActivity.options
} catch (_: IllegalStateException) {
    null
}

internal inline fun launchBiometricRequest(requestId: Int, launch: () -> Unit) {
    try {
        launch()
    } catch (exception: Exception) {
        BiometricRequestStore.remove(requestId)
        throw exception
    }
}

internal fun releaseBiometricRequest(requestId: Int, isChangingConfigurations: Boolean) {
    if (!isChangingConfigurations) {
        BiometricRequestStore.remove(requestId)
    }
}

private fun createBiometricPrompt(activity: FragmentActivity, options: BiometricOptions): BiometricPrompt {
    val (onAuthenticationError, onAuthenticationFailed, onAuthenticationSucceeded) = options
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            activity.finish()
            onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthenticationFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
            onAuthenticationSucceeded(result)
        }
    }
    return BiometricPrompt(activity, callback)
}
