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
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;
import android.util.Log;

// event state spinner handler populates event state spinner with all possible
// event states and reports selected state
public class EventStateSpinnerHandler implements AdapterView.OnItemSelectedListener {

    private Context _context = null;
    private byte _state = Event.State.SCHEDULED;
    // event state mapper instance
    private EventStateMapper _eventStateMapper = null;
    // adapter for event state list
    private ArrayAdapter<String> _eventStateListAdapter = null;

    public EventStateSpinnerHandler(Context context) {
        _context = context;
        _eventStateMapper = new EventStateMapper(_context);
        _eventStateListAdapter = new ArrayAdapter<String>(_context,
            android.R.layout.simple_spinner_item);
        _eventStateListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String stateStr = _eventStateListAdapter.getItem(pos);
        _state = _eventStateMapper.getCode(stateStr);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // reset to default
        _state = Event.State.SCHEDULED;
    }

    // populate event state spinner with state strings
    public void populateSpinner(Spinner spinner) {
        populateSpinner(spinner, Event.State.SCHEDULED);
    }

    // populate event type spinner with type strings and set selection
    public void populateSpinner(Spinner spinner, byte selectedState) {
        spinner.setAdapter(_eventStateListAdapter);
        spinner.setOnItemSelectedListener(this);
        _state = selectedState;
        String selectedStr = _eventStateMapper.getString(_state);

        _eventStateListAdapter.clear();
        String[] strs = _eventStateMapper.getStrings();
        for (String str : strs) {
            if (str.equals(_eventStateMapper.getString(EventStateMapper.ALL_EVENTS_STATE))) {
                continue;
            }
            _eventStateListAdapter.add(str);
            if (str.equals(selectedStr)) {
                int pos = _eventStateListAdapter.getPosition(str);
                spinner.setSelection(pos);
            }
        }
    }

    // get currently selected state
    public byte getSelectedState() {
        return _state;
    }
}
