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
import android.app.Activity;
import android.util.Log;
import android.app.Fragment;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.content.Loader;
import android.app.LoaderManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import java.util.Date;
import java.text.DateFormat;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import android.widget.LinearLayout;

public class EditEntryFragment extends Fragment
                               implements CompanySpinnerHandler.OnSelectedListener,
                                          DateTimeHandler.OnDateChangedListener,
                                          ReminderDialog.ReminderSetListener {

    private class ReminderListClickListener implements View.OnClickListener {

        private EventAlarm _alarm = null;

        public ReminderListClickListener(EventAlarm alarm) {
            _alarm = alarm;
        }

        @Override
        public void onClick(View v) {
            // remove alarm from adapter and its view from reminder list
            _eventAlarmAdapter.remove(_alarm);
            LinearLayout reminderList = (LinearLayout)getActivity().
                findViewById(R.id.event_reminder_list);
            reminderList.removeView(v);
        }
    }

    // entry data for each entry type
    private Company _company = null;
    private Event _event = null;
    private Project _project = null;
    private POC _poc = null;

    // company spinner handler instance
    private CompanySpinnerHandler _companySpinnerHandler = null;
    // project state spinner handler instance
    private ProjectStateSpinnerHandler _projectStateSpinnerHandler = null;
    // POC spinner handler instance
    private POCSpinnerHandler _pocSpinnerHandler = null;
    // project spinner handler instance
    private ProjectSpinnerHandler _projectSpinnerHandler = null;
    // currency spinner handler instance
    private CurrencySpinnerHandler _currencySpinnerHandler = null;
    // event type spinner handler instance
    private EventTypeSpinnerHandler _eventTypeSpinnerHandler = null;
    // event state spinner handler instance
    private EventStateSpinnerHandler _eventStateSpinnerHandler = null; 
    // event date
    private Date _eventDate = null;
    // date/time handler instance
    private DateTimeHandler _dateTimeHandler = null;
    // event reminder adapter
    private ArrayAdapter<EventAlarm> _eventAlarmAdapter = null;

    public EditEntryFragment(Company company) {
        _company = company;
    }

    public EditEntryFragment(Event event) {
        _event = event;
    }

    public EditEntryFragment(Project project) {
        _project = project;
    }

    public EditEntryFragment(POC poc) {
        _poc = poc;
    }

    // save changes made by user
    public void save() {
        if (_company != null) {
            saveCompanyData();
        }
        else if (_event != null) {
            saveEventData();
        }
        else if (_project != null) {
            saveProjectData();
        }
        else if (_poc != null) {
            savePOCData();
        }
        else {
            Log.e("[Scriba]", "EditEntryFragment.save() called, but there's no entry data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;

        if (_company != null) {
            view = inflater.inflate(R.layout.add_company, container, false);
        }
        else if (_event != null) {
            view = inflater.inflate(R.layout.add_event, container, false);
        }
        else if (_project != null) {
            view = inflater.inflate(R.layout.add_project, container, false);
        }
        else if (_poc != null) {
            view = inflater.inflate(R.layout.add_poc, container, false);
        }
        else {
            Log.e("[Scriba]", "Cannot create EditEntryFragment view, no data passed to fragment");
        }

        return view;
    }

    @Override
    public void onStart() {
        if (_company != null) {
            populateCompanyView();
        }
        else if (_event != null) {
            populateEventView();
        }
        else if (_project != null) {
            populateProjectView();
        }
        else if (_poc != null) {
            populatePOCView();
        }

        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    // CompanySpinnerHandler.OnSelectedListener implementation
    @Override
    public void onCompanySelected(UUID companyId) {
        // only for event editor
        if (_event != null) {
            // populate poc spinner with people for currently selected company
            Spinner pocSpinner = (Spinner)getActivity().findViewById(R.id.event_poc_spinner);
            _pocSpinnerHandler.load(pocSpinner, companyId, _event.poc_id);

            // populate project spinner with projects for currently selected company
            Spinner projectSpinner = (Spinner)getActivity().findViewById(R.id.event_project_spinner);
            _projectSpinnerHandler.load(projectSpinner, companyId, _event.project_id);
        }
    }

    // DateTimeHandler.OnDateChangedListener implementation
    @Override
    public void onDateChanged(Date newDate) {
        _eventDate = newDate;

        TextView dateText = (TextView)getActivity().findViewById(R.id.event_date_text);
        TextView timeText = (TextView)getActivity().findViewById(R.id.event_time_text);

        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        dateText.setText(dateFormat.format(_eventDate));
        timeText.setText(timeFormat.format(_eventDate));
    }

    // ReminderDialog.ReminderSetListener implementation
    @Override
    public void onReminderSet(byte type, long value) {
        if (_eventAlarmAdapter == null) {
            // this is the first one, create adapter
            _eventAlarmAdapter = new ArrayAdapter<EventAlarm>(getActivity(),
                R.layout.reminder_item, R.id.event_reminder_text);
        }

        EventAlarm alarm = new EventAlarm(getActivity(), value, type, _eventDate.getTime());
        _eventAlarmAdapter.add(alarm);

        // get view and add it to the reminder list
        int pos = _eventAlarmAdapter.getPosition(alarm);
        LinearLayout reminderList = (LinearLayout)getActivity().
            findViewById(R.id.event_reminder_list);
        View alarmView = _eventAlarmAdapter.getView(pos, null, (ViewGroup)reminderList);
        ViewGroup.LayoutParams params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT);
        reminderList.addView(alarmView, params);
        alarmView.setOnClickListener(new ReminderListClickListener(alarm));
    }

    // populate view with company data
    private void populateCompanyView() {
        EditText txt = (EditText)getActivity().findViewById(R.id.company_name_text);
        txt.setText(_company.name);

        txt = (EditText)getActivity().findViewById(R.id.company_jur_name_text);
        txt.setText(_company.jur_name);

        txt = (EditText)getActivity().findViewById(R.id.company_address_text);
        txt.setText(_company.address);

        txt = (EditText)getActivity().findViewById(R.id.company_inn_text);
        txt.setText(_company.inn);

        txt = (EditText)getActivity().findViewById(R.id.company_phonenum_text);
        txt.setText(_company.phonenum);

        txt = (EditText)getActivity().findViewById(R.id.company_email_text);
        txt.setText(_company.email);
    }

    // populate view with event data
    private void populateEventView() {
        EditText txt = (EditText)getActivity().findViewById(R.id.event_descr_text);
        txt.setText(_event.descr);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(getActivity(),
                                                           getLoaderManager(),
                                                           this);
        Spinner companySpinner = (Spinner)getActivity().findViewById(R.id.event_company_spinner);
        _companySpinnerHandler.load(companySpinner, _event.company_id);

        // setup poc spinner
        _pocSpinnerHandler = new POCSpinnerHandler(getActivity(), getLoaderManager());
        // load() for poc spinner is called when company spinner reports selection

        // setup project spinner
        _projectSpinnerHandler = new ProjectSpinnerHandler(getActivity(),
                                                           getLoaderManager());
        // load() for project spinner is called when company spinner reports selection
        
        // setup event type spinner
        _eventTypeSpinnerHandler = new EventTypeSpinnerHandler(getActivity());
        Spinner eventTypeSpinner = (Spinner)getActivity().findViewById(R.id.event_type_spinner);
        _eventTypeSpinnerHandler.populateSpinner(eventTypeSpinner, _event.type);

        // setup event state spinner
        _eventStateSpinnerHandler = new EventStateSpinnerHandler(getActivity());
        Spinner eventStateSpinner = (Spinner)getActivity().findViewById(R.id.event_state_spinner);
        _eventStateSpinnerHandler.populateSpinner(eventStateSpinner, _event.state);

        // event outcome
        txt = (EditText)getActivity().findViewById(R.id.event_outcome_text);
        txt.setText(_event.outcome);

        // setup event date and time
        _eventDate = new Date(_event.timestamp * 1000);
        _dateTimeHandler = new DateTimeHandler(_eventDate,
                                               getActivity(),
                                               getActivity().getFragmentManager(),
                                               this);
        View dateView = getActivity().findViewById(R.id.event_date);
        View timeView = getActivity().findViewById(R.id.event_time);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dateTimeHandler.showDatePicker();
            }
        });
        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dateTimeHandler.showTimePicker();
            }
        });
        // populate date and time text fields - onDateChanged() will do the job
        onDateChanged(_eventDate);

        // setup "add reminder" button
        View reminderView = getActivity().findViewById(R.id.event_add_reminder);
        reminderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReminderDialog dialog = new ReminderDialog(EditEntryFragment.this,
                    getActivity());
                dialog.show(getActivity().getFragmentManager(), "ReminderDialog");
            }
        });
        // create views for already existing reminders
        EventAlarmMgr eventAlarmMgr = new EventAlarmMgr(getActivity());
        Set<Long> existingAlarms = eventAlarmMgr.getAlarms(_event.id);
        if (existingAlarms != null) {
            for (Long ts : existingAlarms) {
                EventAlarm eventAlarm = new EventAlarm(getActivity(), ts.longValue(),
                    _event.timestamp);
                // onReminderSet will create the view for each reminder
                onReminderSet(eventAlarm.getIntervalType(), eventAlarm.getInterval());
            }
        }
    }

    // populate view with project data
    private void populateProjectView() {
        EditText txt = (EditText)getActivity().findViewById(R.id.project_title_text);
        txt.setText(_project.title);

        txt = (EditText)getActivity().findViewById(R.id.project_descr_text);
        txt.setText(_project.descr);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(getActivity(),
                                                           getLoaderManager(),
                                                           this);
        Spinner companySpinner = (Spinner)getActivity().findViewById(R.id.project_company_spinner);
        _companySpinnerHandler.load(companySpinner, _project.company_id);

        // setup project state spinner
        Spinner projStateSpinner = (Spinner)getActivity().findViewById(R.id.project_state_spinner);
        _projectStateSpinnerHandler = new ProjectStateSpinnerHandler(getActivity());
        _projectStateSpinnerHandler.populateSpinner(projStateSpinner, _project.state);

        // setup currency spinner
        Spinner currencySpinner = (Spinner)getActivity().findViewById(R.id.project_currency_spinner);
        _currencySpinnerHandler = new CurrencySpinnerHandler(getActivity());
        _currencySpinnerHandler.populateSpinner(currencySpinner, _project.currency);

        txt = (EditText)getActivity().findViewById(R.id.project_cost_text);
        txt.setText((new Long(_project.cost)).toString());
    }

    // populate view with poc data
    private void populatePOCView() {
        EditText txt = (EditText)getActivity().findViewById(R.id.poc_firstname_text);
        txt.setText(_poc.firstname);

        txt = (EditText)getActivity().findViewById(R.id.poc_secondname_text);
        txt.setText(_poc.secondname);

        txt = (EditText)getActivity().findViewById(R.id.poc_lastname_text);
        txt.setText(_poc.lastname);

        txt = (EditText)getActivity().findViewById(R.id.poc_mobilenum_text);
        txt.setText(_poc.mobilenum);

        txt = (EditText)getActivity().findViewById(R.id.poc_phonenum_text);
        txt.setText(_poc.phonenum);

        txt = (EditText)getActivity().findViewById(R.id.poc_email_text);
        txt.setText(_poc.email);

        txt = (EditText)getActivity().findViewById(R.id.poc_position_text);
        txt.setText(_poc.position);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(getActivity(),
                                                           getLoaderManager(),
                                                           this);
        Spinner companySpinner = (Spinner)getActivity().findViewById(R.id.poc_company_spinner);
        _companySpinnerHandler.load(companySpinner, _poc.company_id);
    }

    // save company modifications
    private void saveCompanyData() {
        EditText txt = (EditText)getActivity().findViewById(R.id.company_name_text);
        String companyName = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.company_jur_name_text);
        String companyJurName = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.company_address_text);
        String companyAddress = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.company_inn_text);
        String companyInn = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.company_phonenum_text);
        String companyPhonenum = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.company_email_text);
        String companyEmail = txt.getText().toString();

        Company company = new Company(_company.id, companyName, companyJurName,
                                      companyAddress, companyInn, companyPhonenum,
                                      companyEmail);

        // update company data
        ScribaDBManager.useDB(getActivity());
        ScribaDB.updateCompany(company);
        ScribaDBManager.releaseDB();
    }

    // save event modifications
    private void saveEventData() {
        EditText txt = (EditText)getActivity().findViewById(R.id.event_descr_text);
        String descr = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.event_outcome_text);
        String outcome = txt.getText().toString();

        UUID companyId = _companySpinnerHandler.getSelectedCompanyId();
        UUID pocId = _pocSpinnerHandler.getSelectedPOCId();
        UUID projectId = _projectSpinnerHandler.getSelectedProjectId();
        byte type = _eventTypeSpinnerHandler.getSelectedType();
        byte state = _eventStateSpinnerHandler.getSelectedState();
        // libscriba expects timestamp in seconds
        long timestamp = _eventDate.getTime() / 1000;

        Event event = new Event(_event.id, descr, companyId, pocId, projectId,
                                type, outcome, timestamp, state);
        ScribaDBManager.useDB(getActivity());
        ScribaDB.updateEvent(event);
        ScribaDBManager.releaseDB();

        saveEventReminders(event);
    }

    // save event reminder modifications
    private void saveEventReminders(Event event) {
        EventAlarmMgr evtAlarmMgr = new EventAlarmMgr(getActivity());

        if (_eventAlarmAdapter != null) {
            // remove all alarms and add only those that are present
            // in the alarm adapter
            evtAlarmMgr.removeAlarms(event.id);
            for (int i = 0; i < _eventAlarmAdapter.getCount(); i++) {
                EventAlarm alarm = _eventAlarmAdapter.getItem(i);
                // event timestamp may have changed since EventAlarm object
                // creation, so make new object with updated event time
                EventAlarm updAlarm = new EventAlarm(getActivity(),
                    alarm.getInterval(), alarm.getIntervalType(), event.timestamp);
                evtAlarmMgr.addAlarm(event.id, updAlarm.getAlarmTimestamp());
            }
        }
        // if the alarm adapter is not instantiated at this moment, it
        // means there were no reminders set for this event, so do nothing
    }

    // save project modifications
    private void saveProjectData() {
        EditText txt = (EditText)getActivity().findViewById(R.id.project_title_text);
        String title = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.project_descr_text);
        String descr = txt.getText().toString();

        UUID selectedCompanyId = _companySpinnerHandler.getSelectedCompanyId();
        byte selectedState = _projectStateSpinnerHandler.getSelectedState();
        byte selectedCurrency = _currencySpinnerHandler.getSelectedCurrency();

        txt = (EditText)getActivity().findViewById(R.id.project_cost_text);
        long cost = Long.parseLong(txt.getText().toString(), 10);

        long mod_time = _project.mod_time;
        // if project state has changed, update mod_time
        if (_projectStateSpinnerHandler.hasChanged()) {
            mod_time = (new Date()).getTime() / 1000;   // convert to seconds
        }

        Project project = new Project(_project.id, title, descr,
                                      selectedCompanyId, selectedState,
                                      selectedCurrency, cost,
                                      _project.start_time, mod_time);

        ScribaDBManager.useDB(getActivity());
        ScribaDB.updateProject(project);
        ScribaDBManager.releaseDB();
    }

    // save poc modifications
    private void savePOCData() {
        EditText txt = (EditText)getActivity().findViewById(R.id.poc_firstname_text);
        String firstname = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_secondname_text);
        String secondname = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_lastname_text);
        String lastname = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_mobilenum_text);
        String mobilenum = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_phonenum_text);
        String phonenum = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_email_text);
        String email = txt.getText().toString();

        txt = (EditText)getActivity().findViewById(R.id.poc_position_text);
        String position = txt.getText().toString();

        UUID selectedCompanyId = _companySpinnerHandler.getSelectedCompanyId();

        POC poc = new POC(_poc.id, firstname, secondname, lastname, mobilenum,
                          phonenum, email, position, selectedCompanyId);

        ScribaDBManager.useDB(getActivity());
        ScribaDB.updatePOC(poc);
        ScribaDBManager.releaseDB();
    }
}
