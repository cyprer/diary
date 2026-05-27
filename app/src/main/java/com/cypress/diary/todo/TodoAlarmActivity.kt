package com.cypress.diary.todo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class TodoAlarmActivity : Activity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        configureWindow()

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "待办提醒" }
        val note = intent.getStringExtra(EXTRA_NOTE).orEmpty()
        startRinging()
        startVibration()
        setContentView(alarmView(title, note))
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun configureWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startRinging() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
            }
            play()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val activeVibrator = vibrator ?: return
        if (!activeVibrator.hasVibrator()) return
        val pattern = longArrayOf(0L, 800L, 300L, 800L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activeVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            activeVibrator.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun alarmView(title: String, note: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(24, 30, 42))

            addView(TextView(context).apply {
                text = "待办闹钟"
                setTextColor(Color.WHITE)
                textSize = 24f
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 30f
                gravity = Gravity.CENTER
                setPadding(0, 28, 0, 8)
            })
            if (note.isNotBlank()) {
                addView(TextView(context).apply {
                    text = note
                    setTextColor(Color.rgb(220, 226, 235))
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 28)
                })
            }
            addView(Button(context).apply {
                text = "关闭闹钟"
                setOnClickListener {
                    stopAlarm()
                    finish()
                }
            })
        }
    }

    companion object {
        private const val EXTRA_TITLE = "todo_alarm_title"
        private const val EXTRA_NOTE = "todo_alarm_note"

        fun intent(context: Context, title: String, note: String): Intent {
            return Intent(context, TodoAlarmActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_NOTE, note)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
