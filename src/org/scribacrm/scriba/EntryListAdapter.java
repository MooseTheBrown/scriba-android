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
import java.util.ArrayList;
import android.widget.ListAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.database.DataSetObserver;
import android.widget.CheckedTextView;
import java.util.UUID;

// adapter for scriba entry lists
public class EntryListAdapter implements ListAdapter {

    // list of entries
    protected ArrayList<DataDescriptor> _entries = null;
    // list of observers
    protected ArrayList<DataSetObserver> _observers = null;
    protected LayoutInflater _inflater = null;

    public EntryListAdapter(LayoutInflater inflater) {
        _entries = new ArrayList<DataDescriptor>();
        _observers = new ArrayList<DataSetObserver>();
        _inflater = inflater;
    }

    public void add(DataDescriptor entry) {
        _entries.add(entry);
        for (DataSetObserver obs : _observers) {
            obs.onChanged();
        }
    }

    public void clear() {
        _entries.clear();
        for (DataSetObserver obs : _observers) {
            obs.onInvalidated();
        }
    }

    // ListAdapter implementation

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public DataDescriptor getItem(int position) {
        return _entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        DataDescriptor entry = _entries.get(position);
        return entry.id.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

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

        return view;
    }
    
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return _entries.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        _observers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        for (DataSetObserver obs : _observers) {
            if (obs.equals(observer)) {
                _observers.remove(obs);
            }
        }
    }
}
