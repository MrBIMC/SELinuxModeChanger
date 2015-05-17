package com.mrbimc.selinux;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

public class SELinuxBroadcastReceiver extends BroadcastReceiver {

    public static final int PERMISSIVE = 0;
    public static final int ENFORCING = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int selinux = PreferenceManager.getDefaultSharedPreferences(context).getInt("selinux", 3);
        if (selinux == PERMISSIVE){ permiss(context); }
        if (selinux == ENFORCING){ enforce(context); }
    }

    public static void enforce(final Context context) {
        Command command = new Command(0, "/system/bin/setenforce 1") {
            @Override
            public void commandCompleted(int arg0, int arg1) { notifyStateChanged(context, ENFORCING); }
            @Override
            public void commandOutput(int i, String s) {}
            @Override
            public void commandTerminated(int i, String s) {}
        };
        executeCommand(command, context);
    }

    public static void permiss(final Context context){
        Command command = new Command(0, "/system/bin/setenforce 0") {
            @Override
            public void commandCompleted(int arg0, int arg1) { notifyStateChanged(context, PERMISSIVE);}
            @Override
            public void commandOutput(int i, String s) {}
            @Override
            public void commandTerminated(int i, String s) {}
        };
        executeCommand(command, context);
    }

    static void notifyStateChanged(Context context, int state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("selinux", state).apply();

        if (sp.getInt("notify", 0) == 1) {
            int message = state == PERMISSIVE ?
                    R.string.selinux_set_to_permissive : R.string.selinux_set_to_enforcing;
            showNotification(context, context.getString(message));
        }
    }

    static void executeCommand(Command command, Context context){
        try {
            RootTools.getShell(true).add(command);
        } catch (Exception e) {
            e.printStackTrace();
            showNotification(context, context.getString(R.string.no_root_access));
        }
    }

    private static void showNotification(Context context, String message){
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        Notification.Builder mBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(false);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

}
