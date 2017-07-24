package com.mrbimc.selinux

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.mrbimc.selinux.util.KEY_SELINUX_STATE
import com.mrbimc.selinux.util.SELinuxState
import com.mrbimc.selinux.util.setSELinuxState

/**
 * Created by Pavel Sikun on 23.07.17.
 */

class SELinuxBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val selinuxState = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_SELINUX_STATE, SELinuxState.UNKNOWN.value)
        when (selinuxState) {
            0 -> context.setSELinuxState(SELinuxState.PERMISSIVE)
            1 -> context.setSELinuxState(SELinuxState.ENFORCING)
        }
    }
}
