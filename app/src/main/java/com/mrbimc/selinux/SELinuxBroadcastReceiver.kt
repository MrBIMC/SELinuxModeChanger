package com.mrbimc.selinux

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.mrbimc.selinux.util.*

/**
 * Created by Pavel Sikun on 23.07.17.
 */

class SELinuxBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (!sp.getBoolean(KEY_AUTOSTART, true)) {
            return
        }

        val selinuxState = sp.getInt(KEY_SELINUX_STATE, SELinuxState.UNKNOWN.value)
        when (selinuxState) {
            0 -> context.setSELinuxState(SELinuxState.PERMISSIVE)
            1 -> context.setSELinuxState(SELinuxState.ENFORCING)
            else -> context.createNotification(context.getString(R.string.no_root_access))
        }
    }
}
