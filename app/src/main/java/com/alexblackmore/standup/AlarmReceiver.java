package com.alexblackmore.standup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNELTHING = "blargon";
    public static final int NOTIF_ID = 1337;
    NotificationManager notifMgr;
    String title;
    String text;

    void deliverNotif(Context context) {

        Intent myIntent = new Intent(context, MainActivity.class);
        PendingIntent myPI = PendingIntent.getActivity(context, NOTIF_ID, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, CHANNELTHING);

        notifBuilder
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(myPI)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notifMgr.notify(NOTIF_ID, notifBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        title = intent.getStringExtra(Intent.EXTRA_TITLE);
        text = intent.getStringExtra(Intent.EXTRA_TEXT);
        notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        deliverNotif(context);
    }
}
