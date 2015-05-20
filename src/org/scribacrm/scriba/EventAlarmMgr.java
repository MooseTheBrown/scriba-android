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

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlarmManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.net.Uri;
import android.util.Log;

/*
 * EventAlarmMgr handles event alarms.
 * Each alarm is identified by event id and timestamp.
 * The manager stores alarm data in SharedPreferences file and
 * sets/cancels alarms in AlarmManager.
 */
public class EventAlarmMgr {

    // timestamp query parameter key for alarm Uri
    private static final String TIMESTAMP_URI_KEY = "timestamp";

    private Context _context = null;
    private SharedPreferences _prefs = null;
    private AlarmManager _almMgr = null;

    public EventAlarmMgr(Context context) {
        _context = context;
        _prefs = _context.getSharedPreferences("event_alarms", Context.MODE_PRIVATE);
        _almMgr = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
    }

    // add new alarm for given event
    public void addAlarm(UUID eventId, long timestamp) {
        String uuidStr = eventId.toString();
        Set<String> alarms = _prefs.getStringSet(uuidStr, null);
        Set<String> updAlarms = new HashSet<String>();
        // add alarm only if there is no existing alarm set for given time
        boolean exists = false;

        if (alarms == null) {
            updAlarms.add(Long.toString(timestamp));
        }
        else {
            for (String alarm : alarms) {
                long val = Long.parseLong(alarm, 10);
                if (val == timestamp) {
                    exists = true;
                    break;
                }
                updAlarms.add(alarm);
            }

            if (exists == false) {
                updAlarms.add(Long.toString(timestamp));
            }
        }

        if (exists == false) {
            _prefs.edit().putStringSet(uuidStr, updAlarms).apply();
            _almMgr.set(AlarmManager.RTC_WAKEUP, timestamp, buildIntent(eventId, timestamp));
        }
    }

    public void removeAlarm(UUID eventId, long timestamp) {
        String uuidStr = eventId.toString();
        Set<String> alarms = _prefs.getStringSet(uuidStr, null);
        Set<String> updAlarms = new HashSet<String>();
        if (alarms == null) {
            // no alarms set
            return;
        }

        for (String alarm : alarms) {
            long val = Long.parseLong(alarm, 10);
            if (val == timestamp) {
                // cancel alarm in AlarmManager
                _almMgr.cancel(buildIntent(eventId, timestamp));
            }
            else {
                updAlarms.add(alarm);
            }
        }

        // store remaining alarms
        _prefs.edit().putStringSet(uuidStr, updAlarms).apply();
    }

    // used by alarm broadcast receiver to remove alarm that has been just delivered
    public void removeAlarm(Intent alarmIntent) {
        Uri data = alarmIntent.getData();
        EntryURI entryURI = new EntryURI(data);

        // verify entry type
        if (entryURI.getType() != EntryType.EVENT) {
            Log.e("[Scriba]", "EventAlarmMgr.removeAlarm() got invalid entry type: " +
                  entryURI.getType());
            return;
        }

        // get event id
        UUID eventId = entryURI.getId();
        if (eventId == null) {
            Log.e("[Scriba]", "EventAlarmMgr.removeAlarm() got null eventID");
            return;
        }

        // get timestamp
        String tsStr = data.getQueryParameter(TIMESTAMP_URI_KEY);
        if (tsStr == null) {
            Log.e("[Scriba]", "EventAlarmMgr.removeAlarm() got null timestamp");
            return;
        }
        long timestamp = Long.parseLong(tsStr, 10);

        removeAlarm(eventId, timestamp);
    }

    // restore pending alarms in AlarmManager upon device boot
    public void restoreAlarms() {
        Set<String> keyset = _prefs.getAll().keySet();
        for (String key : keyset) {
            UUID eventId = UUID.fromString(key);
            Set<String> alarms = _prefs.getStringSet(key, null);
            for (String alarm : alarms) {
                long timestamp = Long.parseLong(alarm, 10);
                _almMgr.set(AlarmManager.RTC_WAKEUP, timestamp, buildIntent(eventId, timestamp));
            }
        }
    }

    // get alarms for given event
    public Set<Long> getAlarms(UUID eventId) {
        String uuidStr = eventId.toString();
        Set<String> alarms = _prefs.getStringSet(uuidStr, null);

        if (alarms == null) {
            return null;
        }

        Set<Long> timestamps = new HashSet<Long>();
        for (String alarm : alarms) {
            long val = Long.parseLong(alarm, 10);
            timestamps.add(val);
        }

        return timestamps;
    }

    // create PendingIntent to pass it to AlarmManager
    private PendingIntent buildIntent(UUID eventId, long timestamp) {
        EntryURI entryURI = new EntryURI(EntryType.EVENT, eventId);
        // intents always target EventAlarmReceiver, they differ only in data
        Intent intent = new Intent(_context, EventAlarmReceiver.class);
        Uri entry_uri = entryURI.getUri();
        // add timestamp as query parameter to entry URI
        Uri.Builder builder = entry_uri.buildUpon();
        builder.appendQueryParameter(TIMESTAMP_URI_KEY, Long.toString(timestamp));
        intent.setData(builder.build());

        PendingIntent pendingIntent =
            PendingIntent.getBroadcast(_context, 0, intent, 0);

        return pendingIntent;
    }
}
