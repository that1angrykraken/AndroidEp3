package seamonster.kraken.androidep3

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private var shouldStop = true

    companion object {
        const val TAG = "AlarmService"
        private const val CHANNEL_ID = "FOREGROUND_SERVICE"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.tucadonka)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != "STOP") {
            startForeground(NOTIFICATION_ID, notification())
            val shouldRepeat = intent.getBooleanExtra("shouldRepeat", false)
            mediaPlayer.isLooping = shouldRepeat
            Log.d(TAG, "onStartCommand: should repeat = $shouldRepeat")
            mediaPlayer.start()
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun notification(): Notification {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        ).setName("Foreground service").build()
        NotificationManagerCompat.from(this).run {
            createNotificationChannel(channel)
        }

        val intent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Wake up!")
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        stopSelf()
        Log.d(TAG, "onDestroy: service stopped")
    }

    override fun onBind(intent: Intent): IBinder? = null
}