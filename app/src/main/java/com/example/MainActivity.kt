package com.example

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.example.notification.NotificationHelper
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnchorViewModel
import com.example.viewmodel.AnchorTab

private const val TAG_APPCHECK = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

class MainActivity : ComponentActivity() {

    private val viewModel: AnchorViewModel by viewModels()
    private var focusCaptureFromShortcut by mutableStateOf(false)
    private val voiceCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.let { spokenText -> viewModel.capture(spokenText) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // Create notification channels
        NotificationHelper.createChannels(this)

        // Handle incoming Share Target or App Shortcuts Intent
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                LaunchedEffect(Unit) {
                    checkClipboard()
                }

                MainScreen(
                    viewModel = viewModel,
                    focusCaptureRequested = focusCaptureFromShortcut,
                    onCaptureFocusHandled = { focusCaptureFromShortcut = false }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkClipboard()
        viewModel.refreshScheduledAlarms()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun checkClipboard() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val item = clip.getItemAt(0)
                    val text = item.text?.toString()?.trim()
                    if (!text.isNullOrBlank() && text.length > 2) {
                        viewModel.onClipboardDetected(text)
                    }
                }
            }
        } catch (e: Exception) {
            // Clipboard access safe check
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.getStringExtra("action")) {
            "open_morning_brief" -> {
                viewModel.openMorningBrief()
                return
            }
            "open_evening_review" -> {
                viewModel.openEveningReview()
                return
            }
            "new_task" -> {
                viewModel.setTab(AnchorTab.TODAY)
                focusCaptureFromShortcut = true
                return
            }
        }

        when (intent.action) {
            Intent.ACTION_SEND -> {
                // Handle shared text/URL
                if ("text/plain" == intent.type) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.capture(sharedText)
                    }
                } else if (intent.type?.startsWith("image/") == true) {
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    val caption = intent.getStringExtra(Intent.EXTRA_TEXT) ?: "Shared Image"
                    viewModel.capture(caption, sharedUri = imageUri?.toString())
                }
            }
            Intent.ACTION_VIEW -> {
                val actionExtra = intent.getStringExtra("action")
                if (actionExtra == "voice_capture") {
                    // Trigger voice prompt or auto-focus
                    try {
                        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your task or note...")
                        }
                        if (speechIntent.resolveActivity(packageManager) != null) {
                            voiceCaptureLauncher.launch(speechIntent)
                        } else {
                            android.widget.Toast.makeText(
                                this,
                                "Voice input is unavailable on this device",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (_: Exception) {
                        android.widget.Toast.makeText(
                            this,
                            "Voice input is unavailable on this device",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        } else {
            try {
                FirebaseApp.getInstance()
            } catch (_: Exception) {
                null
            }
        }

        if (app == null) {
            Log.d(TAG_APPCHECK, "FirebaseApp is not initialized. App Check skipped.")
            return
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = app.persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Use commit() synchronously so the secret is persisted before provider reads it
                prefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
                Log.d(TAG_APPCHECK, "App Check debug secret configured successfully.")
            }

            FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG_APPCHECK, "DebugAppCheckProviderFactory installed.")
        } else {
            val siteKey = BuildConfig.recaptcha_site_key.trim()
            if (siteKey.isBlank() || siteKey == "RECAPTCHA_SITE_KEY_PLACEHOLDER") {
                Log.e(TAG_APPCHECK, "Release App Check is not configured: set recaptcha_site_key in .env.")
                return
            }
            FirebaseAppCheck.getInstance(app).installAppCheckProviderFactory(
                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            )
            Log.d(TAG_APPCHECK, "RecaptchaAppCheckProviderFactory installed.")
        }
    } catch (e: Exception) {
        Log.e(TAG_APPCHECK, "App Check initialization bypassed or failed", e)
    }
}

