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

import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.app.Dialog;

// event reminder dialog handling
public class ReminderDialog extends DialogFragment {

    public interface ReminderSetListener {
        void onReminderSet(byte type, long value);
    }

    ReminderSetListener _listener = null;
    Context _context = null;
    long _value = 5;
    byte _type = EventAlarm.INTERVAL_MINUTES;
    ArrayAdapter<CharSequence> _typeAdapter = null;
    String _minutesTypeStr = null;
    String _hoursTypeStr = null;
    EditText _valueText = null;

    public ReminderDialog(ReminderSetListener listener, Context context) {
        _listener = listener;
        _context = context;

        // prepare interval type adapter
        _typeAdapter = ArrayAdapter.createFromResource(_context, R.array.event_reminder_list,
            android.R.layout.simple_spinner_item);
        _typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _minutesTypeStr = _context.getResources().getString(R.string.event_reminder_minutes);
        _hoursTypeStr = _context.getResources().getString(R.string.event_reminder_hours);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create alert builder, set title
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle(R.string.event_reminder_title);

        // inflate and set up content view
        LayoutInflater inflater = (LayoutInflater)_context.getSystemService
            (Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.reminder_dialog, null);
        _valueText = (EditText)content.findViewById(R.id.reminder_time);
        _valueText.setText(Long.toString(_value));
        Spinner valueTypeSpinner = (Spinner)content.findViewById(R.id.reminder_spinner);
        valueTypeSpinner.setAdapter(_typeAdapter);
        valueTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String)_typeAdapter.getItem(pos);
                if (selected.equals(_minutesTypeStr)) {
                    _type = EventAlarm.INTERVAL_MINUTES;
                }
                else if (selected.equals(_hoursTypeStr)) {
                    _type = EventAlarm.INTERVAL_HOURS;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setView(content);

        // set up buttons
        builder.setPositiveButton(R.string.event_reminder_ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    _value = Integer.parseInt(_valueText.getText().toString());
                    _listener.onReminderSet(_type, _value);
                }
            });
        builder.setNegativeButton(R.string.event_reminder_cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // do nothing
                }
            });

        return builder.create();
    }
}
