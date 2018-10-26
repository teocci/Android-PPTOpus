package com.github.teocci.android.pptopus.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

import com.github.teocci.android.pptopus.R;

import static com.github.teocci.android.pptopus.utils.Config.NOTIFICATION_CHANNEL_RTSP;

/**
 * Helper class to manage notification channels, and create notifications.
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-02
 */
public class NotificationHelper extends ContextWrapper
{
    private NotificationManager manager;

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx)
    {
        super(ctx);

        // For API 26+ create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_RTSP,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setDescription(getString(R.string.channel_description));
            getManager().createNotificationChannel(channel);
        }
    }
    /**
     * Cancel a previously shown notification.  If it's transient, the view
     * will be hidden.  If it's persistent, it will be removed from the status
     * bar.
     *
     * @param id    The ID of the notification
     */
    public void remove(int id){
        manager.cancel(id);
    }

    /**
     * Get a notification of type 1
     *
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    public Notification getNotification()
    {
        return getNotification(getTitle(), getBody()).build();
    }

    /**
     * Get a notification of type 1
     * <p>
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param title the title of the notification
     * @param body  the body text for the notification
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    public Notification.Builder getNotification(String title, String body)
    {
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setOngoing(true)  // Persistent notification!
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL_RTSP); // Channel ID
        }

        return builder;
    }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification notification)
    {
        getManager().notify(id, notification);
    }

    /**
     * Send a notification.
     *
     * @param id      The ID of the notification
     * @param builder The notification builder
     */
    public void notify(int id, Notification.Builder builder)
    {
        getManager().notify(id, builder.build());
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager()
    {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        return manager;
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon()
    {
        return R.mipmap.ic_smart_audio_noti_icon;
    }

    /**
     * Get the notification title for this app
     *
     * @return The notification title as string
     */
    private String getTitle()
    {
        return getString(R.string.notification_title);
    }

    /**
     * Get the notification content for this app
     *
     * @return The notification content as string
     */
    private String getBody()
    {
        return getString(R.string.notification_content);
    }
}