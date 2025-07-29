package br.com.vansschedule

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.activity.R
import br.com.bean.ShoppingList
import br.com.dao.ShoppingListDAO.select
import br.com.vansact.AddItemShoppingList
import br.com.vansexception.VansException
import java.util.Calendar

class AlarmNotificationShoppingList : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(ACTION) ?: return
        when (action) {
            START_TAG -> notificate(context, intent)
            GOTO_TAG -> gotoShoppingList(context, intent)
            STOP_TAG -> stopNotification(context, intent)
        }
    }

    private fun getShoppingList(context: Context, intent: Intent): ShoppingList? {
        val id = intent.getIntExtra(ID_TAG, -1)
        if (id <= 0) return null
        return try {
            select(context, id)
        } catch (e: VansException) {
            logError("Failed to select shopping list", e)
            null
        }
    }

    private fun notificate(context: Context, intent: Intent) {
        val shoppingList = getShoppingList(context, intent) ?: return
        createNotificationChannel(context)
        val bigIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_action_bar)
            .setContentTitle(shoppingList.name)
            .setLargeIcon(bigIcon)
            .setContentText(context.getString(R.string.notification_arrived))
            .setLights(-0x8000, 1000, 1000)
            .setContentIntent(getPendingIntent(context, shoppingList.id, GOTO_TAG))
            .setVibrate(longArrayOf(0, 500, 1000, 500))
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(
            shoppingList.id,
            builder.build()
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shopping List Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for scheduled shopping lists"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun stopNotification(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ID_TAG, -1)
        if (id <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        getPendingIntent(context, id, STOP_TAG)?.let {
            alarmManager?.cancel(it)
            notificationManager?.cancel(id)
        }
    }

    private fun gotoShoppingList(context: Context, intent: Intent) {
        stopNotification(context, intent)
        val shoppingList = getShoppingList(context, intent) ?: return
        val resultIntent = Intent(context, AddItemShoppingList::class.java).apply {
            putExtra(context.getString(R.string.id_shopping_list), shoppingList.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(resultIntent)
    }

    companion object {
        private const val ACTION = "ACTION"
        private const val ID_TAG = "ID"
        private const val START_TAG = "START"
        private const val STOP_TAG = "STOP"
        private const val GOTO_TAG = "GOTO"
        private const val CHANNEL_ID = "shopping_list_channel"

        @JvmStatic
        fun initAlarm(context: Context, idShoppingList: Int, calendar: Calendar) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            getPendingIntent(context, idShoppingList, START_TAG)?.let {
                alarmManager?.set(AlarmManager.RTC, calendar.timeInMillis, it)
            }
        }

        fun cancelAlarm(context: Context, idShoppingList: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            getPendingIntent(context, idShoppingList, STOP_TAG)?.let {
                alarmManager?.set(AlarmManager.RTC, System.currentTimeMillis(), it)
            }
        }

        private fun getPendingIntent(
            context: Context?,
            id: Int,
            actionValue: String?
        ): PendingIntent? {
            if (context == null) return null
            val i = Intent(context, AlarmNotificationShoppingList::class.java).apply {
                putExtra(ACTION, actionValue)
                putExtra(ID_TAG, id)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getBroadcast(context, id, i, flags)
        }

        private fun logError(message: String, throwable: Throwable) {
            android.util.Log.e("AlarmNotification", message, throwable)
        }
    }
}
