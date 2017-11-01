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

        val selinuxInt = sp.getInt(KEY_SELINUX_STATE, SELinuxState.NOROOT.value)
        when (selinuxInt) {
            SELinuxState.PERMISSIVE.value -> context.setSELinuxState(SELinuxState.PERMISSIVE)
            SELinuxState.ENFORCING.value -> context.setSELinuxState(SELinuxState.ENFORCING)
            SELinuxState.NOROOT.value -> context.createNotification(context.getString(R.string.no_root_access))
            SELinuxState.UNSWITCHABLE.value -> context.createNotification(context.getString(R.string.unswitchable_context))
        }
    }
}
