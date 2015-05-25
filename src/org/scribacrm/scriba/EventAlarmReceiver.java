/*
 * Copyright (C) 2014 Mikhail Sapozhnikov
 *
 * This file is part of scriba-android.
 *
 * scriba-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * scriba-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with scriba-android. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.scribacrm.scriba;

import org.scribacrm.libscriba.*;
import android.content.BroadcastReceiver;
import java.util.Date;
import java.text.DateFormat;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.util.Log;
import android.content.Context;
import java.util.UUID;

// EventAlarmReceiver receives event alarm broadcasts from the system AlarmManager
public class EventAlarmReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("[Scriba]", "EventAlarmReceiver.onReceive()");
        // event alarm has been triggered
        Event event = getEventData(context, intent);
        if (event == null) {
            Log.e("[Scriba]", "EventAlarmReceiver failed to get event from intent");
            Log.e("[Scriba]", "intent URI: " + intent.getData().toString());
            return;
        }

        // show notification
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(event.descr);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        builder.setContentText(timeFormat.format(new Date(event.timestamp)));
        builder.setSmallIcon(R.drawable.scriba_icon);
        // notification intent is almost the same as the one from argument
        Intent activityIntent = new Intent(intent);
        activityIntent.setClass(context, EntryListActivity.class);
        activityIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(context, 0, activityIntent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Notification notification = builder.build();

        NotificationManager nm = (NotificationManager)
                                 context.getSystemService(Context.NOTIFICATION_SERVICE);
        // use event description as notification tag so that different event
        // alarms are not mixed
        nm.notify(event.descr, NOTIFICATION_ID, notification);

        // tell alarm manager to remove this alarm
        EventAlarmMgr mgr = new EventAlarmMgr(context);
        mgr.removeAlarm(intent);
    }

    private Event getEventData(Context context, Intent intent) {
        EntryURI entryUri = new EntryURI(intent.getData());
        EntryType type = entryUri.getType();
        if (type != EntryType.EVENT) {
            return null;
        }
        UUID eventId = entryUri.getId();
        if (eventId == null) {
            return null;
        }

        ScribaDBManager.useDB(context);
        Event event = ScribaDB.getEvent(eventId);
        ScribaDBManager.releaseDB();

        return event;
    }
}
