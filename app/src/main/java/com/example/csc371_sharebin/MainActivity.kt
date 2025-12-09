package com.example.csc371_sharebin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.csc371_sharebin.data.BinRepository
import com.example.csc371_sharebin.data.local.ShareBinDatabase
import com.example.csc371_sharebin.ui.BinViewModel
import com.example.csc371_sharebin.ui.navigation.AppNavGraph
import com.example.csc371_sharebin.ui.theme.CSC371_ShareBinTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.csc371_sharebin.data.UserSession


/**
 * Main entry point for the ShareBin app.
 * Sets up the database, repository, ViewModel, navigation,
 * and handles notification permissions + reminder logic.
 */

class MainActivity : ComponentActivity() {


    private val viewModelFactory by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            ShareBinDatabase::class.java,
            "sharebin.db"
        ).build()

        val repository = BinRepository(db.binDao())

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BinViewModel::class.java)) {
                    return BinViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }


    private val vm: BinViewModel by viewModels { viewModelFactory }

    /**
     * Initializes the app UI, navigation graph, ViewModel, and notification
     * channel. Also requests notification permissions on Android 13+.
     *
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        val startOnMap = intent?.getBooleanExtra("open_map", false) == true

        val isUserLoggedIn = UserSession.isLoggedIn(this)

        setContent {
            CSC371_ShareBinTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    viewModel = vm,
                    startOnMap = startOnMap
                )
            }
        }
    }

    /**
     * Called whenever the activity returns to the foreground.
     * Triggers a check for stale (unverified) favorite bins.
     */
    // 👇 add this
    override fun onResume() {
        super.onResume()
        checkForStaleBinsAndNotify()
    }


    // NOTIFICATIONS
    /**
     * Creates the ShareBin notification channel on Android O+.
     * Required so the app can send verification reminder notifications.
     */

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sharebin_reminders",
                "ShareBin Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Scans all bins and notifies the user if any favorite bins
     * haven't been verified in a long time.
     */
    private fun checkForStaleBinsAndNotify() {
        lifecycleScope.launch {
            val bins = vm.bins.value

            val staleFavorites = bins.filter { bin ->
                bin.isFavorite && isStale(bin.lastVerifiedAt)
            }

            if (staleFavorites.isNotEmpty()) {
                showStaleNotification(staleFavorites.size)
            }
        }
    }

    /**
     * Returns true if the last verification timestamp is older than 30 days.
     *
     */

    private fun isStale(lastVerifiedAt: Long?): Boolean {
        if (lastVerifiedAt == null) return true
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - lastVerifiedAt > thirtyDaysMs
    }

    /**
     * Builds and shows a notification reminding the user to check their bins.
     * Tapping the notification opens the map screen.
     *
     */

    private fun showStaleNotification(count: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_map", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "sharebin_reminders")
            .setSmallIcon(R.drawable.a_pin) // your pin/app icon
            .setContentTitle("ShareBin reminder")
            .setContentText("$count favorite bins haven’t been checked in a while.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(1001, notification)
    }

}
