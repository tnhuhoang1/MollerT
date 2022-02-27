package com.tnh.mollert.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tnh.mollert.R
import com.tnh.tnhlibrary.utils.VersionUtils
import kotlin.random.Random

class NotificationHelper private constructor(
    private val context: Context
){


    fun createSimpleNotification(
        channelId: String,
        title: String,
        content: String
    ): Notification{
        val builder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.app_icon)
            setContentTitle(title)
            setContentText(content)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }
        return builder.build()
    }

    fun createNotification(
        channelId: String,
        builder: NotificationCompat.Builder.() -> Notification
    ): Notification{
        return builder(
            NotificationCompat.Builder(context, channelId)
        )
    }

    fun simpleShowNotification(
        channelId: String,
        channelName: String,
        content: String,
        title: String = context.getString(R.string.app_name),
        notificationId: Int = Random.nextInt()
    ){
        createChannel(channelId, channelName)
        showNotification(
            notificationId,
            createSimpleNotification(channelId, title, content)
        )
    }

    fun showNotification(
        notificationId: Int,
        notification: Notification
    ){
        with(NotificationManagerCompat.from(context)){
            notify(
                notificationId,
                notification
            )
        }
    }

    fun createChannel(
        channelId: String,
        channelName: String,
        desc: String = ""
    ){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = desc
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    companion object{

        fun get(context: Context): NotificationHelper{
            return NotificationHelper(context)
        }

        const val CHANNEL_INVITATION_ID = "invitation_id"
        const val CHANNEL_INVITATION_NAME = "Invitation"
        const val CHANNEL_DEFAULT_ID = "default_id"
        const val CHANNEL_DEFAULT_NAME = "Default"
    }
}