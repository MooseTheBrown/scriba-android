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

import org.scribacrm.libscriba.*;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;
import java.util.UUID;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;

public class EventListAdapter extends EntryListAdapter {

    private Context _context = null;

    public EventListAdapter(Context context, LayoutInflater inflater) {
        super(inflater);
        _context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = _inflater.inflate(R.layout.event_list_entry, parent, false);
        }

        CheckedTextView textView = (CheckedTextView)view.findViewById(R.id.event_list_item_text);
        TextView smallText = (TextView)view.findViewById(R.id.event_list_item_text1);
        LinearLayout layout = (LinearLayout)view.findViewById(R.id.event_list_entry_container);

        DataDescriptor entry = _entries.get(position);
        textView.setText(entry.descr);

        Event event = getEvent(entry.id);
        switch (event.state) {
            case Event.State.COMPLETED:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state completed");
                layout.setBackgroundResource(R.drawable.event_completed);
                break;
            case Event.State.CANCELLED:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state cancelled");
                layout.setBackgroundResource(R.drawable.event_cancelled);
                break;
            default:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state scheduled");
                layout.setBackgroundResource(R.drawable.entry_item_bg);
                break;
        }

        // display event date and time
        Date date = new Date(event.timestamp * 1000); // convert to ms
        boolean showDate = true;
        Calendar cal = Calendar.getInstance();
        int cur_year = cal.get(Calendar.YEAR);
        int cur_month = cal.get(Calendar.MONTH);
        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(date);
        int evt_year = cal.get(Calendar.YEAR);
        int evt_month = cal.get(Calendar.MONTH);
        int evt_day = cal.get(Calendar.DAY_OF_MONTH);
        if ((cur_year == evt_year) && (cur_month == evt_month) &&
            (cur_day == evt_day)) {
            // don't show the date if it is today
            showDate = false;
        }

        DateFormat format = null;
        if (showDate) {
            format = DateFormat.getDateTimeInstance();
        }
        else {
            format = DateFormat.getTimeInstance(DateFormat.SHORT);
        }

        smallText.setText(format.format(date));

        return view;
    }

    // get event by id
    private Event getEvent(UUID eventId) {
        ScribaDBManager.useDB(_context);
        Event event = ScribaDB.getEvent(eventId);
        ScribaDBManager.releaseDB();
        return event;
    }
}
