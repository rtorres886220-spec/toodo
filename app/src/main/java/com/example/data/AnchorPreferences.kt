package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class AnchorSettings(
    val morningTime: String = "08:00",
    val eveningTime: String = "20:00",
    val quietHours: String = "22:00 - 07:00",
    val escalationIntensity: String = "Standard",
    val deadAppHours: Int = 6
)

class AnchorPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("anchor_user_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MORNING_TIME = "key_morning_time"
        private const val KEY_EVENING_TIME = "key_evening_time"
        private const val KEY_QUIET_HOURS = "key_quiet_hours"
        private const val KEY_INTENSITY = "key_intensity"
        private const val KEY_DEAD_APP = "key_dead_app"
        private const val KEY_LAST_ACTIVE = "key_last_active"

        @Volatile
        private var INSTANCE: AnchorPreferences? = null

        fun get(context: Context): AnchorPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnchorPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var morningBriefTime: String
        get() = prefs.getString(KEY_MORNING_TIME, "08:00") ?: "08:00"
        set(value) = prefs.edit().putString(KEY_MORNING_TIME, value).apply()

    var eveningReviewTime: String
        get() = prefs.getString(KEY_EVENING_TIME, "20:00") ?: "20:00"
        set(value) = prefs.edit().putString(KEY_EVENING_TIME, value).apply()

    var quietHours: String
        get() = prefs.getString(KEY_QUIET_HOURS, "22:00 - 07:00") ?: "22:00 - 07:00"
        set(value) = prefs.edit().putString(KEY_QUIET_HOURS, value).apply()

    var escalationIntensity: String
        get() = prefs.getString(KEY_INTENSITY, "Standard") ?: "Standard"
        set(value) = prefs.edit().putString(KEY_INTENSITY, value).apply()

    var deadAppHours: Int
        get() = prefs.getInt(KEY_DEAD_APP, 6)
        set(value) = prefs.edit().putInt(KEY_DEAD_APP, value).apply()

    fun getSettings(): AnchorSettings {
        return AnchorSettings(
            morningTime = morningBriefTime,
            eveningTime = eveningReviewTime,
            quietHours = quietHours,
            escalationIntensity = escalationIntensity,
            deadAppHours = deadAppHours
        )
    }

    fun saveSettings(settings: AnchorSettings) {
        morningBriefTime = settings.morningTime
        eveningReviewTime = settings.eveningTime
        quietHours = settings.quietHours
        escalationIntensity = settings.escalationIntensity
        deadAppHours = settings.deadAppHours
    }

    fun updateLastActiveTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_ACTIVE, timestamp).apply()
    }

    fun getLastActiveTime(): Long {
        return prefs.getLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
    }
}
