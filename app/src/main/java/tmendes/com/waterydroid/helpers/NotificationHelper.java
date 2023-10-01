/*
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tmendes.com.waterydroid.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import tmendes.com.waterydroid.R;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager notificationManager;

    private static final String CHANNEL_ONE_ID = "tmendes.com.waterydroid.CHANNELONE";
    private String CHANNEL_ONE_NAME;

    private final Context ctx;

    public NotificationHelper(Context ctx) {
        super(ctx);
        this.ctx = ctx;
        CHANNEL_ONE_NAME = ctx.getResources().getString(R.string.channelName);
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String notificationsNewMessageRingtone = prefs.getString("notifications_new_message_ringtone", "");
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationsNewMessageRingtone.length() > 0) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                notificationChannel.setSound(Uri.parse(notificationsNewMessageRingtone), audioAttributes);
            }

            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public Notification.Builder getNotification(String title,
                                                String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColorized(true)
                    .setColor(Color.BLUE)
                    .setShowWhen(true)
                    .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher))
                    .setSmallIcon(R.drawable.ic_stat_local_drink)
                    .setAutoCancel(true);
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String notificationsNewMessageRingtone = prefs.getString("notifications_new_message_ringtone", "");
            //noinspection deprecation
            Notification.Builder notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher))
                    .setSmallIcon(R.drawable.ic_stat_local_drink)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                notification.setShowWhen(true);
            }

            if (notificationsNewMessageRingtone.length() > 0) {
                //noinspection deprecation
                notification.setSound(Uri.parse(notificationsNewMessageRingtone));
            }
            return notification;
        }
    }

    private boolean shallNotify() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean notificationsNewMessage = prefs.getBoolean("notifications_new_message", true);
        boolean doNotDisturbOff = true;

        long startTimestamp = prefs.getLong("pref_notification_start", 0);
        long stopTimestamp = prefs.getLong("pref_notification_stop", 0);

        if (startTimestamp > 0 && stopTimestamp > 0) {
            Date now = Calendar.getInstance().getTime();

            Date start = new Date(startTimestamp);
            Date stop = new Date(stopTimestamp);

            doNotDisturbOff = (compareTimes(now, start) >= 0 && compareTimes(now, stop) <= 0);
        }

        return notificationsNewMessage && doNotDisturbOff;
    }

    /* Thanks to:
     * https://stackoverflow.com/questions/7676149/compare-only-the-time-portion-of-two-dates-ignoring-the-date-part
    */
    private long compareTimes(Date currentTime, Date timeToRun) {
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentTime);

        Calendar runCal = Calendar.getInstance();
        runCal.setTime(timeToRun);
        runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
        runCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
        runCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));

        return currentCal.getTimeInMillis() - runCal.getTimeInMillis();
    }

    public void notify(long id, Notification.Builder notification) {
        if (shallNotify()) {
            getManager().notify((int) id, notification.build());
        } else {
            Log.i("WateryDroid", "dnd period");
        }
    }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
}
