package com.dieam.reactnativepushnotification.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.annotation.TargetApi;
import android.content.Intent;
import android.app.IntentService;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;

import com.facebook.common.logging.FLog;

import me.leolin.shortcutbadger.Badger;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Helper for setting application launcher icon badge counts.
 * This is a wrapper around {@link ShortcutBadger}:
 */
public class ApplicationBadgeHelper extends IntentService {

    public static final ApplicationBadgeHelper INSTANCE = new ApplicationBadgeHelper();

    private static final String LOG_TAG = "ApplicationBadgeHelper";

    private static final String NOTIFICATION_CHANNEL = "com.dieam.reactnativepushnotification.helpers";

    private int notificationId = 0;

    private Boolean applyAutomaticBadger;
    private ComponentName componentName;

    private NotificationManager mNotificationManager;

    private ApplicationBadgeHelper() {
        super("ApplicationBadgeHelper");
    }

    public void setApplicationIconBadgeNumber(Context context, int number) {
        if (null == componentName) {
            componentName = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent();
        }
        tryAutomaticBadge(context, number);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int badgeCount = intent.getIntExtra("badgeCount", 0);

            mNotificationManager.cancel(notificationId);
            notificationId++;

            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    .setContentTitle("")
                    .setContentText("");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupNotificationChannel();

                builder.setChannelId(NOTIFICATION_CHANNEL);
            }

            Notification notification = builder.build();
            ShortcutBadger.applyNotification(getApplicationContext(), notification, badgeCount);
            mNotificationManager.notify(notificationId, notification);
        }
    }

    private void tryAutomaticBadge(Context context, int number) {
        if (null == applyAutomaticBadger) {
            applyAutomaticBadger = ShortcutBadger.applyCount(context, number);
            if (applyAutomaticBadger) {
                FLog.i(LOG_TAG, "First attempt to use automatic badger succeeded; permanently enabling method.");
            } else {
                FLog.i(LOG_TAG, "First attempt to use automatic badger failed; permanently disabling method.");
            }
            return;
        } else if (!applyAutomaticBadger) {
            return;
        }
        ShortcutBadger.applyCount(context, number);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "ShortcutBadger Sample",
                NotificationManager.IMPORTANCE_DEFAULT);

        mNotificationManager.createNotificationChannel(channel);
    }
}
