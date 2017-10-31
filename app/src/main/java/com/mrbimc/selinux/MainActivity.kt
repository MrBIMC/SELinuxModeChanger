package com.mrbimc.selinux

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.yarolegovich.lovelydialog.LovelyProgressDialog
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.mrbimc.selinux.util.*
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlinx.android.synthetic.main.layout_command.*
import kotlinx.android.synthetic.main.layout_context.*
import kotlinx.android.synthetic.main.layout_notifications.*
import kotlinx.android.synthetic.main.layout_selinux_state.*
import kotlinx.android.synthetic.main.layout_autostart.*

class MainActivity : AppCompatActivity() {

    val sp by lazy { PreferenceManager.getDefaultSharedPreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkSELinuxState()
        checkCommand()
        checkContext()
        checkNotificationSettings()
        checkAutoStartSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_about -> showInfoDialog()
        else -> false
    }

    fun setEnforcing(v: View) = setSELinuxState(SELinuxState.ENFORCING) {
        runOnUiThread {
            buttonEnforcing.isEnabled = false
            buttonPermissive.isEnabled = true
        }
    }

    fun setPermissive(v: View) = setSELinuxState(SELinuxState.PERMISSIVE) {
        runOnUiThread {
            buttonEnforcing.isEnabled = true
            buttonPermissive.isEnabled = false
        }
    }

    private fun checkContext() {
        val items = resources.getStringArray(R.array.selinux_contexts)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)


        val selectedItem = sp.getString(KEY_CONTEXT, items[0])
        val selectedItemPosition = adapter.getPosition(selectedItem)

        contextsSpinner.adapter = adapter
        contextsSpinner.setSelection(selectedItemPosition)

        contextsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /*IGNORE*/ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sp.edit().putString(KEY_CONTEXT, items[position]).apply()
            }
        }
    }

    private fun checkCommand() {
        val items = resources.getStringArray(R.array.selinux_commands)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        val selectedItem = sp.getString(KEY_COMMAND, items[0])
        val selectedItemPosition = adapter.getPosition(selectedItem)

        commandsSpinner.adapter = adapter
        commandsSpinner.setSelection(selectedItemPosition)

        commandsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /*IGNORE*/ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sp.edit().putString(KEY_COMMAND, items[position]).apply()
            }
        }
    }

    private fun checkNotificationSettings() {
        notificationsSwitch.isChecked = sp.getBoolean(KEY_NOTIFICATIONS, true)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply()
        }
    }

    private fun checkAutoStartSettings() {
        autoStartSwitch.isChecked = sp.getBoolean(KEY_AUTOSTART, true)

        autoStartSwitch.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_AUTOSTART, isChecked).apply()
        }
    }

    private fun checkSELinuxState() {

        val ad = LovelyProgressDialog(this)
                .setTopColorRes(R.color.colorAccent)
                .setTopTitle(getString(R.string.requesting_root))
                .setTopTitleColor(ContextCompat.getColor(this, android.R.color.white))
                .setIcon(R.drawable.security).show()

        getSELinuxState() {
            ad.cancel()

            when (it) {
                SELinuxState.UNKNOWN -> {
                    runOnUiThread {
                        buttonEnforcing.isEnabled = false
                        buttonPermissive.isEnabled = false
                    }
                }
                SELinuxState.PERMISSIVE -> {
                    runOnUiThread {
                        buttonEnforcing.isEnabled = true
                        buttonPermissive.isEnabled = false
                    }
                }
                SELinuxState.ENFORCING -> {
                    runOnUiThread {
                        buttonEnforcing.isEnabled = false
                        buttonPermissive.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showInfoDialog(): Boolean {
        LovelyStandardDialog(this)
                .setTopColorRes(R.color.colorAccent)
                .setTopTitle(getString(R.string.about_this_app))
                .setTopTitleColor(ContextCompat.getColor(this, android.R.color.white))
                .setButtonsColorRes(R.color.colorAccent)
                .setIcon(R.drawable.information)
                .setMessage(R.string.info_dialog_message)
                .setNegativeButton(getString(R.string.button_close_dialog), null)
                .setPositiveButton(getString(R.string.button_open_github)) {
                    openGithub()
                }
                .show()

        return true
    }

    private fun openGithub() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MrBIMC/SELinuxModeChanger")))
    }

}
