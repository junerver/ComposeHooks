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
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import kotlin.properties.Delegates
import xyz.junerver.compose.hooks.utils._useSetState
import xyz.junerver.kotlin.Tuple2

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
fun useBiometric(
    options: BiometricOptions = BiometricOptions(),
): Tuple2<() -> Unit, Boolean> {
    val (isAuthed, setIsAuthed) = _useSetState(default = false)
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        setIsAuthed(it.resultCode == Activity.RESULT_OK)
    }
    val open: () -> Unit = {
        launcher.launch(BiometricActivity.newIntent(context, options))
    }
    return Tuple2(
        open,
        isAuthed.value
    )
}

class BiometricActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(options.title)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText(options.negativeButtonText)
            .build()
        createBiometricPrompt(this, options).authenticate(promptInfo)
    }

    companion object {
        var options: BiometricOptions by Delegates.notNull()
        fun newIntent(
            context: Context,
            options: BiometricOptions,
        ) = Intent(context, BiometricActivity::class.java).also {
            this.options = options
        }
    }
}

private fun createBiometricPrompt(
    activity: FragmentActivity,
    options: BiometricOptions,
): BiometricPrompt {
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
