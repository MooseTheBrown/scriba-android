package org.scribacrm.scriba;

import org.scribacrm.libscriba.*;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;
import android.util.Log;

// event type spinner handler populates event type spinner with all possible
// types and reports selected type
public class EventTypeSpinnerHandler implements AdapterView.OnItemSelectedListener {

    private Context _context = null;
    private byte _type = Event.Type.MEETING;
    // event type mapper instance
    private EventTypeMapper _eventTypeMapper = null;
    // adapter for event type list
    private ArrayAdapter<String> _eventTypeListAdapter = null;

    public EventTypeSpinnerHandler(Context context) {
        _context = context;
        _eventTypeMapper = new EventTypeMapper(_context);
        _eventTypeListAdapter = new ArrayAdapter<String>(_context,
            android.R.layout.simple_spinner_item);
        _eventTypeListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String typeStr = _eventTypeListAdapter.getItem(pos);
        _type = _eventTypeMapper.getCode(typeStr);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // reset to default
        _type = Event.Type.MEETING;
    }

    // populate event type spinner with type strings
    public void populateSpinner(Spinner spinner) {
        populateSpinner(spinner, Event.Type.MEETING);
    }

    // populate event type spinner with type strings and set selection
    public void populateSpinner(Spinner spinner, byte selectedType) {
        spinner.setAdapter(_eventTypeListAdapter);
        spinner.setOnItemSelectedListener(this);
        _type = selectedType;
        String selectedStr = _eventTypeMapper.getString(_type);

        _eventTypeListAdapter.clear();
        String[] strs = _eventTypeMapper.getStrings();
        for (String str : strs) {
            _eventTypeListAdapter.add(str);
            if (str.equals(selectedStr)) {
                int pos = _eventTypeListAdapter.getPosition(str);
                spinner.setSelection(pos);
            }
        }
    }

    // get currently selected state
    public byte getSelectedType() {
        return _type;
    }
}
