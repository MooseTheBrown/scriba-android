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
import android.app.Activity;
import android.os.Bundle;
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;
import android.widget.EditText;
import android.util.Log;
import android.content.Loader;
import android.app.LoaderManager;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import java.util.Date;
import java.text.DateFormat;
import android.widget.TextView;

public class AddEntryActivity extends Activity
                              implements CompanySpinnerHandler.OnSelectedListener,
                                         DateTimeHandler.OnDateChangedListener {

    // keys for Intent extras
    public static final String ENTRY_TYPE_INTENT_KEY = "EntryType";



    // entry type
    private EntryType _entryType = EntryType.COMPANY;
    // company spinner handler
    private CompanySpinnerHandler _companySpinnerHandler = null;
    // project state spinner handler instance
    private ProjectStateSpinnerHandler _projectStateSpinnerHandler = null;
    // POC spinner handler instance
    private POCSpinnerHandler _pocSpinnerHandler = null;
    // Project spinner handler instance
    private ProjectSpinnerHandler _projectSpinnerHandler = null;
    // event type spinner handler instance
    private EventTypeSpinnerHandler _eventTypeSpinnerHandler = null;
    // event state spinner handler instance
    private EventStateSpinnerHandler _eventStateSpinnerHandler = null;
    // date/time handler instance
    private DateTimeHandler _dateTimeHandler = null;
    // event date/time
    private Date _eventDate = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // first try to get entry type from saved state
        if (savedInstanceState != null) {
            int typeId = savedInstanceState.getInt(ENTRY_TYPE_INTENT_KEY, -1);
            if (typeId != -1) {
                setEntryType(typeId);
            }
        }
        else {
            // no saved entry type, get it from intent
            Intent intent = getIntent();
            setEntryType(intent.getIntExtra(ENTRY_TYPE_INTENT_KEY, EntryType.COMPANY.id()));
        }

        // set content corresponding to current entry type
        switch (_entryType) {
            case COMPANY:
                setupCompanyView();
                break;
            case EVENT:
                setupEventView();
                break;
            case POC:
                setupPOCView();
                break;
            case PROJECT:
                setupProjectView();
                break;
        }

        setActionBarTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_entry_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveEntry();
                setResult(RESULT_OK);
                finish();
                return true;
            case R.id.action_cancel:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save current entry type and id
        outState.putInt(ENTRY_TYPE_INTENT_KEY, _entryType.id());
    }

    // company spinner handler calls this when company selection changes
    @Override
    public void onCompanySelected(long companyId) {
        // if we are adding new event, we need to update POC and project spinners
        // with people and projects for currently selected company
        if (_entryType == EntryType.EVENT) {
            if (_pocSpinnerHandler != null) {
                Spinner pocSpinner = (Spinner)findViewById(R.id.event_poc_spinner);
                _pocSpinnerHandler.load(pocSpinner, companyId);
            }
            if (_projectSpinnerHandler != null) {
                Spinner projectSpinner = (Spinner)findViewById(R.id.event_project_spinner);
                _projectSpinnerHandler.load(projectSpinner, companyId);
            }
        }
    }

    // DateTimeHandler.OnDateChangedListener implementation
    @Override
    public void onDateChanged(Date newDate) {
        _eventDate = newDate;

        TextView dateText = (TextView)findViewById(R.id.event_date_text);
        TextView timeText = (TextView)findViewById(R.id.event_time_text);

        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        dateText.setText(dateFormat.format(_eventDate));
        timeText.setText(timeFormat.format(_eventDate));
    }

    // calls save routine for the current entry type
    private void saveEntry() {
        Log.d("[Scriba]", "AddEntryActivity.saveEntry()");

        switch (_entryType) {
            case COMPANY:
                saveCompany();
                break;
            case EVENT:
                saveEvent();
                break;
            case POC:
                savePOC();
                break;
            case PROJECT:
                saveProject();
                break;
        }
    }

    // company save routine
    private void saveCompany() {
        // get text entered by user
        EditText txt = (EditText)findViewById(R.id.company_name_text);
        String companyName = txt.getText().toString();

        txt = (EditText)findViewById(R.id.company_jur_name_text);
        String companyJurName = txt.getText().toString();

        txt = (EditText)findViewById(R.id.company_address_text);
        String companyAddress = txt.getText().toString();

        txt = (EditText)findViewById(R.id.company_inn_text);
        String companyInn = txt.getText().toString();

        txt = (EditText)findViewById(R.id.company_phonenum_text);
        String companyPhonenum = txt.getText().toString();

        txt = (EditText)findViewById(R.id.company_email_text);
        String companyEmail = txt.getText().toString();

        // add new company to the database
        ScribaDBManager.useDB(this);
        ScribaDB.addCompany(companyName, companyJurName, companyAddress,
                            companyInn, companyPhonenum, companyEmail);
        ScribaDBManager.releaseDB();
    }

    // POC save routine
    private void savePOC() {
        EditText txt = (EditText)findViewById(R.id.poc_firstname_text);
        String firstname = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_secondname_text);
        String secondname = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_lastname_text);
        String lastname = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_mobilenum_text);
        String mobilenum = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_phonenum_text);
        String phonenum = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_email_text);
        String email = txt.getText().toString();

        txt = (EditText)findViewById(R.id.poc_position_text);
        String position = txt.getText().toString();

        long companyId = _companySpinnerHandler.getSelectedCompanyId();

        // add new POC to the database
        ScribaDBManager.useDB(this);
        ScribaDB.addPOC(firstname, secondname, lastname, mobilenum, phonenum,
                        email, position, companyId);
        ScribaDBManager.releaseDB();
    }

    // project save routine
    private void saveProject() {
        EditText txt = (EditText)findViewById(R.id.project_title_text);
        String title = txt.getText().toString();

        txt = (EditText)findViewById(R.id.project_descr_text);
        String descr = txt.getText().toString();

        long companyId = _companySpinnerHandler.getSelectedCompanyId();
        byte state = _projectStateSpinnerHandler.getSelectedState();

        // add new project to the database
        ScribaDBManager.useDB(this);
        ScribaDB.addProject(title, descr, companyId, state);
        ScribaDBManager.releaseDB();
    }

    // event save routine
    private void saveEvent() {
        EditText txt = (EditText)findViewById(R.id.event_descr_text);
        String descr = txt.getText().toString();

        long companyId = _companySpinnerHandler.getSelectedCompanyId();
        long pocId = _pocSpinnerHandler.getSelectedPOCId();
        long projectId = _projectSpinnerHandler.getSelectedProjectId();
        byte type = _eventTypeSpinnerHandler.getSelectedType();
        byte state = _eventStateSpinnerHandler.getSelectedState();
        long timestamp = _eventDate.getTime();

        txt = (EditText)findViewById(R.id.event_outcome_text);
        String outcome = txt.getText().toString();
        ScribaDBManager.useDB(this);
        ScribaDB.addEvent(descr, companyId, pocId, projectId, type,
                          outcome, timestamp, state);
        ScribaDBManager.releaseDB();
    }

    // set entry type based on its integer representation
    private void setEntryType(int typeId) {
        if (typeId == EntryType.COMPANY.id()) { _entryType = EntryType.COMPANY; }
        else if (typeId == EntryType.EVENT.id()) { _entryType = EntryType.EVENT; }
        else if (typeId == EntryType.POC.id()) { _entryType = EntryType.POC; }
        else if (typeId == EntryType.PROJECT.id()) { _entryType = EntryType.PROJECT; }
    }

    // view setup routines for different types of entries

    private void setupCompanyView() {
        setContentView(R.layout.add_company);
    }

    private void setupPOCView() {
        setContentView(R.layout.add_poc);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(this, getLoaderManager(), null);
        Spinner spinner = (Spinner)findViewById(R.id.poc_company_spinner);
        _companySpinnerHandler.load(spinner);
    }

    private void setupProjectView() {
        setContentView(R.layout.add_project);

        // setup project state spinner
        Spinner projectStateSpinner = (Spinner)findViewById(R.id.project_state_spinner);
        _projectStateSpinnerHandler = new ProjectStateSpinnerHandler(this);
        _projectStateSpinnerHandler.populateSpinner(projectStateSpinner);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(this, getLoaderManager(), null);
        Spinner companySpinner = (Spinner)findViewById(R.id.project_company_spinner);
        _companySpinnerHandler.load(companySpinner);
    }

    private void setupEventView() {
        setContentView(R.layout.add_event);

        // setup company spinner
        _companySpinnerHandler = new CompanySpinnerHandler(this, getLoaderManager(), this);
        Spinner companySpinner = (Spinner)findViewById(R.id.event_company_spinner);
        _companySpinnerHandler.load(companySpinner);

        // setup POC spinner
        _pocSpinnerHandler = new POCSpinnerHandler(this, getLoaderManager());
        // POC data will be loaded when company spinner reports selection through
        // onCompanySelected()

        // setup project spinner
        _projectSpinnerHandler = new ProjectSpinnerHandler(this, getLoaderManager());
        // project data will be loaded when company spinner reports selection through
        // onCompanySelected()

        // setup event type spinner
        Spinner eventTypeSpinner = (Spinner)findViewById(R.id.event_type_spinner);
        _eventTypeSpinnerHandler = new EventTypeSpinnerHandler(this);
        _eventTypeSpinnerHandler.populateSpinner(eventTypeSpinner);

        // setup event state spinner
        Spinner eventStateSpinner = (Spinner)findViewById(R.id.event_state_spinner);
        _eventStateSpinnerHandler = new EventStateSpinnerHandler(this);
        _eventStateSpinnerHandler.populateSpinner(eventStateSpinner);

        // setup date/time widgets and handler
        // populate date and time fields - onDateChanged() will do the job
        onDateChanged(new Date());
        View dateView = findViewById(R.id.event_date);
        View timeView = findViewById(R.id.event_time);
        _dateTimeHandler = new DateTimeHandler(this, getFragmentManager(), this);
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
    }

    // set action bar title according to current entry type
    private void setActionBarTitle() {
        String title = null;

        switch (_entryType) {
            case COMPANY:
                title = getResources().getString(R.string.add_company);
                break;
            case EVENT:
                title = getResources().getString(R.string.add_event);
                break;
            case PROJECT:
                title = getResources().getString(R.string.add_project);
                break;
            case POC:
                title = getResources().getString(R.string.add_poc);
                break;
        }

        if (title != null) {
            getActionBar().setTitle(title);
        }
    }
}
