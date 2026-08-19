package com.example.uzradyab.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.uzradyab.MainActivity
import com.example.uzradyab.R
import com.example.uzradyab.domain.manager.FcmTokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM_TOKEN", "New FCM Token generated: $token")
        scope.launch {
            fcmTokenManager.syncToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "هشدار جدید", it.body ?: "")
        } ?: run {
            // If the payload is data-only
            val title = remoteMessage.data["title"] ?: "هشدار جدید"
            val body = remoteMessage.data["body"] ?: ""
            if (body.isNotBlank()) {
                sendNotification(title, body)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val rtlTitle = "\u200F$title"
        val rtlBody = "\u200F$messageBody"

        val channelId = "uzradyab_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val appLogo = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Uses vector path for proper white silhouette
            .setLargeIcon(appLogo)
            .setContentTitle(rtlTitle)
            .setContentText(rtlBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Uzradyab Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt() /* ID of notification */, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
