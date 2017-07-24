package com.mrbimc.selinux.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.mrbimc.selinux.MainActivity
import com.mrbimc.selinux.R
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Created by Pavel Sikun on 24.07.17.
 */

fun Context.getSELinuxState(callback: (SELinuxState) -> Unit) = launch(CommonPool) {
    val sp = PreferenceManager.getDefaultSharedPreferences(this@getSELinuxState)
    when(executeGetSELinuxState().get()) {
        SELinuxState.UNKNOWN -> {
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.UNKNOWN.value).apply()
            callback(SELinuxState.UNKNOWN)
        }
        SELinuxState.ENFORCING -> {
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.ENFORCING.value).apply()
            callback(SELinuxState.ENFORCING)
        }
        SELinuxState.PERMISSIVE -> {
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.PERMISSIVE.value).apply()
            callback(SELinuxState.PERMISSIVE)
        }
    }
}

fun Context.setSELinuxState(state: SELinuxState, callback: (SELinuxState) -> Unit = {}) = launch(CommonPool) {
    val sp = PreferenceManager.getDefaultSharedPreferences(this@setSELinuxState)

    val defaultCommand = resources.getStringArray(R.array.selinux_commands)[0]
    val defaultContext = resources.getStringArray(R.array.selinux_contexts)[0]

    val command = sp.getString(KEY_COMMAND, defaultCommand).replace("#STATE#", "${state.value}")
    val context = sp.getString(KEY_CONTEXT, defaultContext)

    var commandToExecute = command

    if (context != defaultContext) {
        commandToExecute = "su --context $context -c \"$command\""
    }

    when(executeSetSELinuxState(state, commandToExecute).get()) {
        SELinuxState.UNKNOWN -> {
            createNotification(getString(R.string.no_root_access))
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.UNKNOWN.value).apply()
            callback(SELinuxState.UNKNOWN)
        }
        SELinuxState.ENFORCING -> {
            createNotification(getString(R.string.selinux_set_to_enforcing))
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.ENFORCING.value).apply()
            callback(SELinuxState.ENFORCING)
        }
        SELinuxState.PERMISSIVE -> {
            createNotification(getString(R.string.selinux_set_to_permissive))
            sp.edit().putInt(KEY_SELINUX_STATE, SELinuxState.PERMISSIVE.value).apply()
            callback(SELinuxState.PERMISSIVE)
        }
    }
}

fun Context.createNotification(message: String) {
    val sp = PreferenceManager.getDefaultSharedPreferences(this)
    if (!sp.getBoolean(KEY_NOTIFICATIONS, true)) {
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel()
    }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

    val notificationID = 1
    val channelID = KEY_NOTIFICATIONS

    val notification = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .build()

    notificationManager.notify(notificationID, notification)
}

@TargetApi(Build.VERSION_CODES.O)
fun Context.createNotificationChannel() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val id = KEY_NOTIFICATIONS
    val name = getString(R.string.channel_name)
    val importance = NotificationManager.IMPORTANCE_UNSPECIFIED

    val channel = NotificationChannel(id, name, importance)
    channel.enableLights(true)
    channel.lightColor = Color.GREEN
    channel.enableVibration(false)
    notificationManager.createNotificationChannel(channel)
}