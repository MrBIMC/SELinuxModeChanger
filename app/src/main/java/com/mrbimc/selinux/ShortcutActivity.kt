package com.mrbimc.selinux

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import com.mrbimc.selinux.util.*

/**
 * Created by Pavel Sikun on 31.10.2017.
 */

class ShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processShortcut(intent)
        finish()
    }

    private fun processShortcut(intent: Intent) {
        val uri = intent.data ?: return

        val host = uri.host
        reportShortcutUsed(host)

        when (host) {
            SHORTCUT_ENFORCING -> {
                setSELinuxState(SELinuxState.ENFORCING)
            }
            SHORTCUT_PERMISSIVE -> {
                setSELinuxState(SELinuxState.PERMISSIVE)
            }
        }
    }

    private fun reportShortcutUsed(shortcutId: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            shortcutManager.reportShortcutUsed(shortcutId)
        }
    }

}
