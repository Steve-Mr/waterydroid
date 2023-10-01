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

package tmendes.com.waterydroid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tmendes.com.waterydroid.helpers.AlarmHelper;

public class BootReceiver extends BroadcastReceiver {

    private final AlarmHelper alarm = new AlarmHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                int notificationFrequency = Integer.parseInt(prefs.getString("notification_frequency", "120"));
                boolean notificationsNewMessage = prefs.getBoolean("notifications_new_message", true);
                alarm.cancelAlarm(context);
                if (notificationsNewMessage) {
                    alarm.setAlarm(context, notificationFrequency);
                }
            }
        }
    }
}
