package com.duke.elliot.kim.java.basicfirebaseexample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        var title = p0.notification?.title
        var body = p0.notification?.body
        showNotification(title, body)
    }

    fun showNotification(title: String?, body: String?) {
        var intent = Intent(this, MainActivity::class.java)
        var pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        var channelId = "fcm_default_channel"
        var sSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var nBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(sSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)

        var nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var oChannel = NotificationChannel(channelId, "Customer", NotificationManager.IMPORTANCE_HIGH)
            nManager.createNotificationChannel(oChannel)
        }

        nManager.notify(0, nBuilder.build())
    }
}