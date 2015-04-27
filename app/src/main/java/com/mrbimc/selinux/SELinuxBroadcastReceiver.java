package com.mrbimc.selinux;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;

public class SELinuxBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int selinux = sp.getInt("selinux", 3);
        if (selinux == 0){ permiss(context); }
        if (selinux == 1){ enforce(context); }
    }


    public static void enforce(final Context ctx) {
        Command command = new Command(0, "/system/bin/setenforce 1") {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

            @Override
            public void commandCompleted(int arg0, int arg1) {
                sp.edit().putInt("selinux", 1).apply();
                if(sp.getInt("notify", 0) == 1) showNotification(ctx, ctx.getString(R.string.selinux_set_to_enforcing));
            }
        };
        executeCommand(command, ctx);
    }

    public static void permiss(final Context ctx) {
        Command command = new Command(0, "/system/bin/setenforce 0") {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

            @Override
            public void commandCompleted(int arg0, int arg1) {
                sp.edit().putInt("selinux", 1).apply();
                if(sp.getInt("notify", 0) == 1) showNotification(ctx, ctx.getString(R.string.selinux_set_to_permissive));
            }
        };
        executeCommand(command, ctx);
    }

    static void executeCommand(Command command, Context context){
        try {
            RootShell.getShell(true).add(command);
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
