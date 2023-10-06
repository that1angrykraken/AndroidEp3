package seamonster.kraken.androidep3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import seamonster.kraken.androidep3.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.timePicker.run {
            setIs24HourView(true)
            binding.textTime.setText("$hour:$minute")
            setOnTimeChangedListener { _, hour, minute ->
                binding.textTime.setText("$hour:$minute")
                Log.d(TAG, "onCreate: $hour:$minute")
            }
        }

        binding.buttonSetAlarm.setOnClickListener {
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            val intent = Intent(this, AlarmService::class.java).apply {
                putExtra("shouldRepeat", binding.cbShouldRepeat.isChecked)
            }
            val pendingIntent =
                PendingIntent.getService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= 31) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            Toast.makeText(this, "Alarm set!", Toast.LENGTH_SHORT).show()
        }
    }
}