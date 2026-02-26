package com.sagealarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.sagealarm.MainActivity
import com.sagealarm.R
import com.sagealarm.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var ttsPlayer: TtsPlayer

    private var player: ExoPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isDismissed = AtomicBoolean(false)
    private var alarmJob: Job? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        alarmJob?.cancel()
        isDismissed.set(false)
        ttsPlayer.initialize()
        startForeground(NOTIFICATION_ID, buildNotification(alarmId))
        launchDismissScreen(alarmId)

        alarmJob = scope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: run { stopSelf(); return@launch }
            val maxRings = if (alarm.repeatCount == -1) Int.MAX_VALUE else alarm.repeatCount

            var ringIndex = 0
            while (ringIndex < maxRings && !isDismissed.get()) {
                if (alarm.isMusicEnabled) {
                    playAlarmSound(alarm.musicUri)
                }
                if (alarm.isTtsEnabled && alarm.ttsMessage.isNotBlank()) {
                    ttsPlayer.speak(alarm.ttsMessage, Locale.getDefault())
                }
                if (alarm.isVibrationEnabled) {
                    startVibration()
                }

                delay(RING_DURATION_MS)
                stopSoundAndVibration()

                if (!isDismissed.get() && ringIndex < maxRings - 1) {
                    delay(alarm.alarmIntervalMinutes * 60_000L)
                }
                ringIndex++
            }

            if (!isDismissed.get()) stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun playAlarmSound(musicUri: String?) {
        player?.stop()
        player?.release()
        player = null

        val uri: Uri = if (!musicUri.isNullOrBlank()) {
            Uri.parse(musicUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 500)
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = v
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, 0)
        }
    }

    private fun stopSoundAndVibration() {
        player?.stop()
        player?.release()
        player = null
        vibrator?.cancel()
    }

    private fun launchDismissScreen(alarmId: Long) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_SHOW_DISMISS, true)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        isDismissed.set(true)
        ttsPlayer.stop()
        ttsPlayer.release()
        player?.stop()
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(alarmId: Long): Notification {
        val dismissIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_SHOW_DISMISS, true)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, (alarmId and 0x7FFF_FFFFL).toInt(), dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(getString(R.string.notification_title))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.alarm_channel_description)
            setBypassDnd(true)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            setSound(null, audioAttributes)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "sage_alarm_channel"
        const val NOTIFICATION_ID = 1001
        private const val RING_DURATION_MS = 60_000L
    }
}
