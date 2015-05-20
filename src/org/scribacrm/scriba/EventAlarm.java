/*
 * Copyright (C) 2015 Mikhail Sapozhnikov
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

import java.util.Date;
import android.content.Context;

public class EventAlarm {

    public static final byte INTERVAL_MINUTES = 0;
    public static final byte INTERVAL_HOURS = 1;

    private Context _context = null;
    // alarm timestamp
    private long _alarm = 0;
    // event timestamp
    private long _event = 0;
    // interval type (kept for presentation purposes)
    private byte _type = INTERVAL_MINUTES;

    public EventAlarm(Context context, long alm, long event) {
        _context = context;
        _alarm = alm;
        _event = event;
    }

    public EventAlarm(Context context, int interval, byte type, long event) {
        _context = context;
        _event = event;
        _type = type;

        if (_type == INTERVAL_MINUTES) {
            _alarm = _event - interval * 60;
        }
        else if (_type == INTERVAL_HOURS) {
            _alarm = _event - interval * 3600;
        }
    }

    public EventAlarm(Context context, int interval, long event) {
        _context = context;
        _event = event;
        _alarm = _event - interval;
        // determine the type automatically
        if ((_alarm >= 3600) && ((_alarm % 3600) == 0)) {
            _type = INTERVAL_HOURS;
        }
        else {
            _type = INTERVAL_MINUTES;
        }
    }

    public long getEventTimestamp() { return _event; }
    public long getAlarmTimestamp() { return _alarm; }
    public byte getIntervalType() { return _type; }

    @Override
    public String toString() {
        long interval = _event - _alarm;
        int resid = 0;

        if (_type == INTERVAL_MINUTES) {
            interval /= 60;
            resid = R.string.event_reminder_minutes;
        }
        else if (_type == INTERVAL_HOURS) {
            interval /= 3600;
            resid = R.string.event_reminder_hours;
        }

        String result = Long.toString(interval) + " " + _context.getResources().getString(resid);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        EventAlarm other = (EventAlarm)obj;
        if ((_event == other.getEventTimestamp()) &&
            (_alarm == other.getAlarmTimestamp()) &&
            (_type == other.getIntervalType())) {
            return true;
        }
        else {
            return false;
        }
    }
}
