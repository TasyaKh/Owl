package com.example.bd.activities.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bd.R
import com.example.bd.activities.MainActivity

class Notification(context: Context,
                   private val title:String, private val text:String) {

    companion object {
      private const val NOTIFICATION_ID = 100
      private const val CHANNEL_ID = "channelID"
    }

   init {

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        // Создаём канал, если версия андроид >= 25
        createChannelIfNeeded(notificationManager)
        val builder =  createStyleNotification(context)

        //при нажатии на уведомление открыть приложение
        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        //получить активность с этого класса
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent,
             PendingIntent.FLAG_UPDATE_CURRENT
        ) //   PendingIntent.IMMUTABLE
        //задать к нашему уведомлению активность, которую будем запускать
        builder.setContentIntent(pendingIntent)
        //собрать уведомление
        val notificationCompat = builder.build()
        //отправить пользователю
        notificationManager.notify(NOTIFICATION_ID, notificationCompat)

        Log.d("intentNotification", " started")
    }



    private fun createStyleNotification(context: Context): NotificationCompat.Builder {
        //построить уведомление
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_owl)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
    }


    private fun createChannelIfNeeded(notificationManager: NotificationManager){

        // Создаём канал, если версия андроид >= 25
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val name: CharSequence = "word priority"
            val description = "show one priority word from dictionary (randomly)"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.apply {
                this.description = description
                enableLights(true)
                setShowBadge(false)
            }


            notificationManager.createNotificationChannel(mChannel)
            //Log.d("importance", (notificationManager.importance).toString())
            //notificationManager.deleteNotificationChannel(CHANNEL_ID)
        }
    }
}