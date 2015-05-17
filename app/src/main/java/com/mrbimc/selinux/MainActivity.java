package com.mrbimc.selinux;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

public class MainActivity extends AppCompatActivity {

    Button bEnforcing, bPermissive;
    CheckBox cNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bEnforcing = (Button) findViewById(R.id.button_enforcing);
        bPermissive = (Button) findViewById(R.id.button_permissive);
        cNotify = (CheckBox) findViewById(R.id.checkbox_notify);

        setupNotify();

        detectRootAccess();
    }

    private void setupNotify() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int notify = sp.getInt("notify", 0);

        if (notify == 1) cNotify.setChecked(true);
        if (notify == 0) cNotify.setChecked(false);

        cNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) sp.edit().putInt("notify", 1).apply();
                else sp.edit().putInt("notify", 0).apply();
            }
        });
    }

    private void detectRootAccess() {
        new AsyncTask<Void, Void, Void>() {
            boolean isRoot = false;
            boolean isAccessGiven = false;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    isRoot = RootTools.isRootAvailable();
                    isAccessGiven = RootTools.isAccessGiven();
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(isRoot && isAccessGiven) detectSELinuxMode();
                else {
                    bEnforcing.setEnabled(false);
                    bPermissive.setEnabled(false);
                    showDialog(getString(R.string.no_root_access), getString(R.string.no_root_access_summary));
                }

            }
        }.execute();
    }

    private void detectSELinuxMode() {

        Command command = new Command(0, "/system/bin/getenforce") {
            int status = 2;

            @Override
            protected void output(int id, String line) {
                super.output(id, line);
                status = line.contains(getString(R.string.selinux_enforcing)) ? 0 : line.contains(getString(R.string.selinux_permissive)) ? 1 : 2;
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                switch (status) {
                    case 0:
                        setEnforcing(null);
                        break;
                    case 1:
                        setPermissive(null);
                        break;
                    case 2:
                        setUndefined();
                        break;
                }
            }

            @Override
            public void commandOutput(int id, String line) {}
            @Override
            public void commandTerminated(int i, String s) {}
        };
        try { RootTools.getShell(true).add(command); } catch (Exception e) { e.printStackTrace(); }
    }

    public void setPermissive(View v){
        if(v != null) SELinuxBroadcastReceiver.permiss(this);
        bEnforcing.setEnabled(true);
        bPermissive.setEnabled(false);
    }

    public void setEnforcing(View v){
        if(v != null) SELinuxBroadcastReceiver.enforce(this);
        bEnforcing.setEnabled(false);
        bPermissive.setEnabled(true);
    }

    private void setUndefined(){
        bEnforcing.setEnabled(false);
        bPermissive.setEnabled(false);
        showDialog(getString(R.string.selinux_unreachable), getString(R.string.selinux_disabled));
    }

    private void showDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok),null);
        builder.create();
        builder.setCancelable(false);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about)
            startActivity(new Intent(this, CreditsActivity.class));
        return super.onOptionsItemSelected(item);
    }
}
