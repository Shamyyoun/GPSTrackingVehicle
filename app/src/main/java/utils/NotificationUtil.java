package utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.mahmoudelshamy.gpstracking.vehicleapp.R;

/**
 * Created by Shamyyoun on 3/29/2015.
 */
public class NotificationUtil {
    /**
     * method, used to show basic simple notification with msgResId and sound uri
     */
    public static void show(Context context, int id, int msgRes, Uri soundUri) {
        String msg = context.getString(msgRes);
        show(context, id, msg, soundUri);
    }

    /**
     * method, used to show basic simple notification with msg and sound uri
     */
    public static void show(Context context, int id, String msg, Uri soundUri) {
        int iconRes = R.drawable.ic_launcher;
        String title = context.getString(R.string.app_name);
        long when = System.currentTimeMillis();

        show(context, id, iconRes, title, msg, when, soundUri, null, null, Notification.FLAG_AUTO_CANCEL);
    }

    /**
     * method, used to show basic simple notification with string message
     */
    public static void show(Context context, int id, String msg) {
        int iconRes = R.drawable.ic_launcher;
        String title = context.getString(R.string.app_name);
        long when = System.currentTimeMillis();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        show(context, id, iconRes, title, msg, when, soundUri, null, null, Notification.FLAG_AUTO_CANCEL);
    }

    /**
     * method, used to show basic simple notification with action
     */
    public static void show(Context context, int id, int msgRes, boolean hasSound, String action) {
        int iconRes = R.drawable.ic_launcher;
        String title = context.getString(R.string.app_name);
        String msg = context.getString(msgRes);
        long when = System.currentTimeMillis();
        Uri soundUri = null;
        if (hasSound) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        show(context, id, iconRes, title, msg, when, soundUri, action, null, Notification.FLAG_AUTO_CANCEL);
    }

    /**
     * method, used to show notification with target activity
     */
    public static void show(Context context, int id, int msgRes, Class targetActivity) {
        int iconRes = R.drawable.ic_launcher;
        String title = context.getString(R.string.app_name);
        String msg = context.getString(msgRes);
        long when = System.currentTimeMillis();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        show(context, id, iconRes, title, msg, when, soundUri, null, targetActivity, Notification.FLAG_AUTO_CANCEL);
    }

    /**
     * method, used to show on going notification with target activity
     */
    public static void showOnGoing(Context context, int id, int msgRes, Class targetActivity) {
        int iconRes = R.drawable.ic_launcher;
        String title = context.getString(R.string.app_name);
        String msg = context.getString(msgRes);
        long when = System.currentTimeMillis();

        show(context, id, iconRes, title, msg, when, null, null, targetActivity, Notification.FLAG_ONGOING_EVENT);
    }


    /**
     * method, used to show notification with passed parameters
     */
    private static void show(Context context, int id, int iconRes, String title, String content, long when, Uri soundUri, String action, Class targetActivity, int flag) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent();
        if (action != null)
            notificationIntent.setAction(action);
        else if (targetActivity != null)
            notificationIntent.setClass(context, targetActivity);
        PendingIntent contentIntent = PendingIntent.getActivity(context, id, notificationIntent, 0);
        Notification notification = new Notification(iconRes, title, when);
        if (soundUri != null)
            notification.sound = soundUri;
        notification.setLatestEventInfo(context, title, content, contentIntent);
        notification.flags |= flag;

        mNotificationManager.notify(id, notification);
    }

    /**
     * method, used to cancel notification by id
     */
    public static void cancel(Context context, int id) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
        mNotificationManager.cancel(id);
    }
}
