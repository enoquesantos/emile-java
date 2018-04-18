package gsort.pos.engsisubiq.EmileMobile;

import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;

public class Notifications {

    private static Context context;
    private static String packageName;
    private static Resources resources;
    private static SharedPreferences sharedPreferences;
    private static String TAG = "Notification";
    private static Notifications instance;

    private Notifications() { }

    public static Notifications getInstance() {
        if (instance == null)
            instance = new Notifications();
        return instance;
    }

    public void initialize(Context context) {
        this.context      = context;
        packageName       = context.getPackageName();
        sharedPreferences = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);

        try {
            PackageManager pm = context.getPackageManager();
            resources = pm.getResourcesForApplication(packageName);
        }  catch(Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    // android system notification has many options!
    // take a look in documentation for more details in follow links:
    // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
    // messageData will be pass to QtApplication as argument when user click in notification
    public void notify(String title, String message, String messageData) {
        Intent intent = null;
        int messageId = new Random().nextInt(99999);
        int badgeNum = sharedPreferences.getInt("badgeNumber", 0);

        // create a new intent to send to the Activity that will be pass to QtApplication.
        // The push message pay load data will be serialized and sent as argument when user click in the notification
        // the messagaData value can be read as a new Intent extra parameter in onResume(...) method
        // this feature works only if the app is opened!
        if (messageData != null) {
            intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("messageData", messageData);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(context);

        // create pending intent, a mention to the Activity which needs to be
        // triggered when user clicks in the notification. In this case is QtActivity class
        if (intent != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, messageId, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationBuilder.setContentIntent(contentIntent);
        }

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentText(message);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        notificationBuilder.setVibrate(new long[] {500, 500, 500});

        // if the notification body text is a large text, show in notification
        // as a big text, available from android lolipop or above
        Notification.BigTextStyle bigText = new Notification.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);

        try {
            // Obs: bigText.setSummaryText can be used to show the message sender name!
            // if (message != null && !message.equals("") && sender != null && !sender.equals(""))
            //    bigText.setSummaryText(":: " + sender);
            notificationBuilder.setStyle(bigText);
        } catch(Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        // send the message to system tray
        notificationManager.notify(messageId, notificationBuilder.build());

        // update the badge number
        BadgeUtils.incrementBadge(context);

        // send the push notification message (messageData as String) to application
        // if the app is opened (background or foreground)
    }
}
