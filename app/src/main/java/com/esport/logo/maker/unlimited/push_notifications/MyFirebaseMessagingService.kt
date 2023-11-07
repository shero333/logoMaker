package com.esport.logo.maker.unlimited.push_notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.ActivityMain
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService(){

    val channelId = "esport_logo_maker_channel"
    val channelName = "com.esport.logo.maker.unlimited.notifications"

    //showing the notification
    override fun onMessageReceived(message: RemoteMessage) {

        if (message.notification != null){
            generateNotification(message.notification!!.title!!,message.notification!!.body!!)
        }
    }

    //setting up the view
    private fun getRemoteView(title: String, message: String): RemoteViews {

        val remoteView = RemoteViews(channelId,R.layout.notification_layout)

        remoteView.setTextViewText(R.id.title,title)
        remoteView.setTextViewText(R.id.description,message)
        remoteView.setImageViewResource(R.id.logo_app,R.drawable.app_icon)

        return remoteView
    }
    //generating notification
    fun generateNotification(title:String, message:String){

        val intent = Intent(this,ActivityMain::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)

        //creating a notification
        var builder = NotificationCompat
            .Builder(this, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //creating channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId, channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(0,builder.build())
    }
}