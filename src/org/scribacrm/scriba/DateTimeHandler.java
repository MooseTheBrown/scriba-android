package org.scribacrm.scriba;

import org.scribacrm.libscriba.*;
import java.util.Date;
import java.util.Calendar;
import android.text.format.DateFormat;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.widget.TimePicker;
import android.widget.DatePicker;
import android.os.Bundle;
import android.content.Context;
import android.app.FragmentManager;
import android.app.Dialog;

// DateTimeHandler manages date/time setting.
// It launches date/time pickers on request and reports
// new date/time set by user.
public class DateTimeHandler {

    public interface OnDateChangedListener {
        void onDateChanged(Date newDate);
    }

    private Date _date = null;
    private Context _context = null;
    private FragmentManager _fragmentManager = null;
    private OnDateChangedListener _listener = null;

    public DateTimeHandler(Context context,
                           FragmentManager fragmentManager,
                           OnDateChangedListener listener) {
        // initialize to current date and time
        _date = new Date();
        _context = context;
        _fragmentManager = fragmentManager;
        _listener = listener;
    }

    public DateTimeHandler(Date date,
                           Context context,
                           FragmentManager fragmentManager,
                           OnDateChangedListener listener) {
        _date = date;
        _context = context;
        _fragmentManager = fragmentManager;
        _listener = listener;
    }

    public void showTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.show(_fragmentManager, "timePicker");
    }

    public void showDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.show(_fragmentManager, "datePicker");
    }

    private class TimePicker extends DialogFragment
                             implements TimePickerDialog.OnTimeSetListener  {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of TimePickerDialog and return it
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_date);
            return new TimePickerDialog(_context, this,
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_date);
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            _date = calendar.getTime();
            _listener.onDateChanged(_date);
        }
    }

    private class DatePicker extends DialogFragment
                             implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_date);
            return new DatePickerDialog(_context, this,
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH));
                                        
        }

        @Override
        public void onDateSet(android.widget.DatePicker view,
                              int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_date);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            _date = calendar.getTime();
            _listener.onDateChanged(_date);
        }
    }
}
