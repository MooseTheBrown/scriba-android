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
import android.util.Log;
import java.util.UUID;

public class EventListAdapter extends EntryListAdapter {

    private Context _context = null;

    public EventListAdapter(Context context, LayoutInflater inflater) {
        super(inflater);
        _context = context;
    }

    /* The only difference between event list view and generic entry
       list view is that events should have different background based
       on their state */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = _inflater.inflate(R.layout.list_entry, parent, false);
        }

        DataDescriptor entry = _entries.get(position);
        CheckedTextView textView = (CheckedTextView)view.findViewById(R.id.list_item_text);
        textView.setText(entry.descr);

        switch (getEventState(entry.id)) {
            case Event.State.COMPLETED:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state completed");
                textView.setBackgroundResource(R.drawable.event_completed);
                break;
            case Event.State.CANCELLED:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state cancelled");
                textView.setBackgroundResource(R.drawable.event_cancelled);
                break;
            default:
                Log.d("[Scriba]", "EventListAdapter.getView(), event state scheduled");
                textView.setBackgroundResource(R.drawable.entry_item_bg);
                break;
        }

        return view;
    }

    // get event state by event id
    private byte getEventState(UUID eventId) {
        ScribaDBManager.useDB(_context);
        Event event = ScribaDB.getEvent(eventId);
        ScribaDBManager.releaseDB();

        if (event == null) {
            return -1;
        }
        return event.state;
    }
}
