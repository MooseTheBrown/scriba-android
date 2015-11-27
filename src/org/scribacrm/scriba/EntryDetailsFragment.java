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
import android.app.LoaderManager;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.AdapterView;
import java.util.Date;
import java.text.DateFormat;
import java.util.UUID;
import java.util.Set;

public class EntryDetailsFragment extends Fragment {

    // interface, which must be implemented by all activities using this fragment
    public interface EntryDetailsActivityInterface {

        // return values for callNumber()
        public static final byte CALL_POSSIBLE = 0;
        public static final byte CALL_IMPOSSIBLE = 1;

        /* callNumber() should attempt to call given phone number.
           If there's no phone app available, it should return
           CALL_IMPOSSIBLE. Otherwise it should return CALL_POSSIBLE.
         */
        byte callNumber(String number);
        EntryType getEntryType();
        UUID getEntryId();
        void onEntryChange(EntryType newType, UUID newId);
        void onFragmentResumed(EntryType type, UUID id);
    }

    // LoaderCallbacks implementation
    private class LoaderCbImpl<EntryType>
                  implements LoaderManager.LoaderCallbacks<EntryType>{
        
        private Class<EntryType> _cls;

        public LoaderCbImpl(Class<EntryType> cls) {
            Log.d("[Scriba]", "init LoaderCbImpl, cls=" + cls);
            _cls = cls;
        }

        @Override
        public Loader<EntryType> onCreateLoader(int id, Bundle args) {
            Loader<EntryType> loader =
            (Loader<EntryType>) new EntryDetailsLoader<EntryType>(getActivity(),
                                                                  _entryId,
                                                                  _cls);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<EntryType> loader, EntryType data) {
            if (data == null) {
                Log.e("[Scriba]", "LoaderCbImpl.onLoadFinished(): requested item not found");
                return;
            }

            if (_cls == Company.class) {
                _company = (Company)data;
                populateCompanyDetails();
            }
            else if (_cls == Event.class) {
                _event = (Event)data;
                populateEventDetails();
            }
            else if (_cls == Project.class) {
                _project = (Project)data;
                populateProjectDetails();
            }
            else if (_cls == POC.class) {
                _poc = (POC)data;
                populatePOCDetails();
            }
        }

        @Override
        public void onLoaderReset(Loader<EntryType> loader) {
            _company = null;
            _event = null;
            _project = null;
            _poc = null;
        }
    }

    // EntryListClickListener handles item clicks in company view POC, project and
    // event lists
    private class EntryListClickListener implements View.OnClickListener {

        private EntryListAdapter _adapter = null;
        private int _pos = -1;
        private EntryType _handlerEntryType;

        public EntryListClickListener(EntryListAdapter adapter,
                                      int pos,
                                      EntryType entryType) {
            _adapter = adapter;
            _pos = pos;
            _handlerEntryType = entryType;
        }

        @Override
        public void onClick(View v) {
            // get new item id using adapter and report entry change event to activity
            DataDescriptor item = _adapter.getItem(_pos);
            _activityInterface.onEntryChange(_handlerEntryType, item.id);
        }
    }

    // PhoneItemHandler is responsible for handling phone icon clicks and
    // hiding phone icon if the phone number is absent
    private class PhoneItemHandler {

        private int _phoneViewRes = -1;
        private int _phoneIconRes = -1;
        private String _phonenum = null;

        public PhoneItemHandler(int phoneViewRes,
                                int phoneIconRes,
                                String phonenum) {
            _phoneViewRes = phoneViewRes;
            _phoneIconRes = phoneIconRes;
            _phonenum = phonenum;

            View phoneView = getActivity().findViewById(_phoneViewRes);
            if (phonenum != null) {
                phoneView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _activityInterface.callNumber(_phonenum);
                        // TODO: check return value and display
                        // alert dialog if call is not possible
                    }
                });
            }
            else {
                phoneView.setClickable(false);
                View phoneIconView = getActivity().findViewById(_phoneIconRes);
                phoneIconView.setVisibility(View.INVISIBLE);
            }
        }
    }

    // activity interface
    private EntryDetailsActivityInterface _activityInterface = null;
    // entry type
    private EntryType _entryType = EntryType.COMPANY;
    // entry id
    private UUID _entryId = null;

    // currently loaded entry data
    private Company _company = null;
    private Event _event = null;
    private Project _project = null;
    private POC _poc = null;

    // list adapters for entry lists inside company view
    private EntryListAdapter _poc_list_adapter = null;
    private EntryListAdapter _project_list_adapter = null;
    private EventListAdapter _event_list_adapter = null;

    // adapter for event reminders
    private ArrayAdapter<EventAlarm> _eventAlarmAdapter = null;

    public Company getCompany() { return _company; }
    public Event getEvent() { return _event; }
    public Project getProject() { return _project; }
    public POC getPOC() { return _poc; }

    @Override
    public void onAttach(Activity activity) {
        Log.d("[Scriba]", "EntryDetailsFragment.onAttach()");
        _activityInterface = (EntryDetailsActivityInterface)activity;
        _entryType = _activityInterface.getEntryType();
        _entryId = _activityInterface.getEntryId();

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("[Scriba]", "EntryDetailsFragment.onCreateView()");

        View view = null;

        switch (_entryType) {
            case COMPANY:
                view = inflater.inflate(R.layout.company_details, container, false);
                break;
            case EVENT:
                view = inflater.inflate(R.layout.event_details, container, false);
                break;
            case PROJECT:
                view = inflater.inflate(R.layout.project_details, container, false);
                break;
            case POC:
                view = inflater.inflate(R.layout.poc_details, container, false);
                break;
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadEntryDetails();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("[Scriba]", "EntryDetailsFragment.onResume()");
        _activityInterface.onFragmentResumed(_entryType, _entryId);
    }

    // initiate loading of entry details
    private void loadEntryDetails() {
        switch (_entryType) {
            case COMPANY:
                getLoaderManager().restartLoader(0, null, new LoaderCbImpl<Company>(Company.class));
                break;
            case EVENT:
                getLoaderManager().restartLoader(0, null, new LoaderCbImpl<Event>(Event.class));
                break;
            case PROJECT:
                getLoaderManager().restartLoader(0, null, new LoaderCbImpl<Project>(Project.class));
                break;
            case POC:
                getLoaderManager().restartLoader(0, null, new LoaderCbImpl<POC>(POC.class));
                break;
        }
    }

    // populate UI controls with company data received from Loader
    private void populateCompanyDetails() {
        Log.d("[Scriba]", "populateCompanyDetails()");

        if (_company == null) {
            // there's no data
            Log.e("[Scriba]", "populateCompanyDetails() called with null _company");
            return;
        }

        // verify that company id is the same as requested
        if (!_entryId.equals(_company.id)) {
            Log.e("[Scriba]", "Requested company details for id=" + _entryId.toString() +
                              ", but received details for id=" + _company.id.toString());
            return;
        }

        TextView txt = (TextView)getActivity().findViewById(R.id.company_name_text);
        txt.setText(_company.name);

        txt = (TextView)getActivity().findViewById(R.id.company_jur_name_text);
        txt.setText(_company.jur_name);

        txt = (TextView)getActivity().findViewById(R.id.company_address_text);
        txt.setText(_company.address);

        txt = (TextView)getActivity().findViewById(R.id.company_inn_text);
        txt.setText(_company.inn);

        txt = (TextView)getActivity().findViewById(R.id.company_phonenum_text);
        txt.setText(_company.phonenum);
        new PhoneItemHandler(R.id.company_phonenum,
                             R.id.company_phonenum_button,
                             _company.phonenum);

        txt = (TextView)getActivity().findViewById(R.id.company_email_text);
        txt.setText(_company.email);

        populateCompanyLists();
    }

    // populate POC, project and event lists in company view
    private void populateCompanyLists() {
        // populate POC list
        _poc_list_adapter = new EntryListAdapter(getActivity().getLayoutInflater());

        if (_company.poc_list != null) {
            for (DataDescriptor poc : _company.poc_list) {
                _poc_list_adapter.add(poc);
            }
        }
        LinearLayout poc_list = (LinearLayout)getActivity().findViewById(R.id.poc_list);
        for (int i = 0; i < _poc_list_adapter.getCount(); i++) {
            // add entry
            View entryView = _poc_list_adapter.getView(i, null, (ViewGroup)poc_list);
            ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            poc_list.addView(entryView, params);
            entryView.setOnClickListener(new EntryListClickListener(_poc_list_adapter,
                                                                    i,
                                                                    EntryType.POC));
        }

        // populate project list
        _project_list_adapter = new EntryListAdapter(getActivity().getLayoutInflater());

        if (_company.proj_list != null) {
            for (DataDescriptor project : _company.proj_list) {
                _project_list_adapter.add(project);
            }
        }
        LinearLayout project_list = (LinearLayout)getActivity().findViewById(R.id.project_list);
        for (int i = 0; i < _project_list_adapter.getCount(); i++) {
            // add entry
            View entryView = _project_list_adapter.getView(i, null, (ViewGroup)project_list);
            ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            project_list.addView(entryView, params);
            entryView.setOnClickListener(new EntryListClickListener(_project_list_adapter,
                                                                    i,
                                                                    EntryType.PROJECT));
        }

        // populate event list
        _event_list_adapter = new EventListAdapter(getActivity(),
            getActivity().getLayoutInflater());

        if (_company.event_list != null) {
            for (DataDescriptor event : _company.event_list) {
                _event_list_adapter.add(event);
            }
        }
        LinearLayout event_list = (LinearLayout)getActivity().findViewById(R.id.event_list);
        for (int i = 0; i < _event_list_adapter.getCount(); i++) {
            // add entry
            View entryView = _event_list_adapter.getView(i, null, (ViewGroup)event_list);
            ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            event_list.addView(entryView, params);
            entryView.setOnClickListener(new EntryListClickListener(_event_list_adapter,
                                                                    i,
                                                                    EntryType.EVENT));
        }

        // configure expandable panel containing the POC list
        ExpandablePanel pocPanel = (ExpandablePanel)getActivity().findViewById(R.id.poc_panel);
        ExpandablePanel.OnExpandListener pocPanelListener = 
            new ExpandablePanel.OnExpandListener() {
                @Override
                public void onExpand(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.poc_exp_image);
                    expandImage.setImageResource(R.drawable.up);
                }

                @Override
                public void onCollapse(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.poc_exp_image);
                    expandImage.setImageResource(R.drawable.down);
                }
            };
        pocPanel.setOnExpandListener(pocPanelListener);

        // configure expandable panel containing the project list
        ExpandablePanel projectPanel = (ExpandablePanel)getActivity().
                                       findViewById(R.id.project_panel);
        ExpandablePanel.OnExpandListener projectPanelListener = 
            new ExpandablePanel.OnExpandListener() {
                @Override
                public void onExpand(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.project_exp_image);
                    expandImage.setImageResource(R.drawable.up);
                }

                @Override
                public void onCollapse(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.project_exp_image);
                    expandImage.setImageResource(R.drawable.down);
                }
            };
        projectPanel.setOnExpandListener(projectPanelListener);

        // configure expandable panel containing the event list
        ExpandablePanel eventPanel = (ExpandablePanel)getActivity().
                                     findViewById(R.id.event_panel);
        ExpandablePanel.OnExpandListener eventPanelListener = 
            new ExpandablePanel.OnExpandListener() {
                @Override
                public void onExpand(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.event_exp_image);
                    expandImage.setImageResource(R.drawable.up);
                }

                @Override
                public void onCollapse(View handle, View content) {
                    ImageView expandImage = (ImageView)getActivity().
                                               findViewById(R.id.event_exp_image);
                    expandImage.setImageResource(R.drawable.down);
                }
            };
        eventPanel.setOnExpandListener(eventPanelListener);
    }

    // populate UI controls with event data received from Loader
    private void populateEventDetails() {
        Log.d("[Scriba]", "populateEventDetails()");

        if (_event == null) {
            // there's no data
            Log.e("[Scriba]", "populateEventDetails() called with null _event");
            return;
        }

        // verify that event id is the same as requested
        if (!_entryId.equals(_event.id)) {
            Log.e("[Scriba]", "Requested event details for id=" + _entryId.toString() +
                              ", but received details for id=" + _event.id.toString());
            return;
        }

        // get company, POC and project data for the event
        ScribaDBManager.useDB(getActivity());
        Company company = ScribaDB.getCompany(_event.company_id);
        POC poc = ScribaDB.getPoc(_event.poc_id);
        Project project = ScribaDB.getProject(_event.project_id);
        ScribaDBManager.releaseDB();

        TextView txt = (TextView)getActivity().findViewById(R.id.event_descr_text);
        txt.setText(_event.descr);

        if (company != null) {
            txt = (TextView)getActivity().findViewById(R.id.event_company_text);
            txt.setText(company.name);
            View event_company = getActivity().findViewById(R.id.event_company);
            event_company.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ask activity to show company details
                    _activityInterface.onEntryChange(EntryType.COMPANY, _event.company_id);
                }
            });
        }
        if (poc != null) {
            txt = (TextView)getActivity().findViewById(R.id.event_poc_text);
            String name = poc.firstname;
            if (poc.secondname != null) {
                name += " " + poc.secondname;
            }
            if (poc.lastname != null) {
                name += " " + poc.lastname;
            }
            txt.setText(name);
            View event_poc = getActivity().findViewById(R.id.event_poc);
            event_poc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ask activity to show POC details
                    _activityInterface.onEntryChange(EntryType.POC, _event.poc_id);
                }
            });
        }
        if (project != null) {
            txt = (TextView)getActivity().findViewById(R.id.event_project_text);
            txt.setText(project.title);
            View event_project = getActivity().findViewById(R.id.event_project);
            event_project.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ask activity to show project details
                    _activityInterface.onEntryChange(EntryType.PROJECT, _event.project_id);
                }
            });
        }

        EventTypeMapper eventTypeMapper = new EventTypeMapper(getActivity());
        String typeStr = eventTypeMapper.getString(_event.type);
        txt = (TextView)getActivity().findViewById(R.id.event_type_text);
        txt.setText(typeStr);

        EventStateMapper eventStateMapper = new EventStateMapper(getActivity());
        String stateStr = eventStateMapper.getString(_event.state);
        txt = (TextView)getActivity().findViewById(R.id.event_state_text);
        txt.setText(stateStr);

        txt = (TextView)getActivity().findViewById(R.id.event_outcome_text);
        txt.setText(_event.outcome);

        // event date and time
        Date date = new Date(_event.timestamp * 1000); // convert to ms
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        txt = (TextView)getActivity().findViewById(R.id.event_date_text);
        txt.setText(dateFormat.format(date));

        txt = (TextView)getActivity().findViewById(R.id.event_time_text);
        txt.setText(timeFormat.format(date));

        populateEventReminders();
    }

    // populate list of event reminders
    private void populateEventReminders() {
        if (_event == null) {
            return;
        }

        _eventAlarmAdapter = new ArrayAdapter<EventAlarm>(getActivity(),
            R.layout.reminder_item, R.id.event_reminder_text);

        EventAlarmMgr alarmMgr = new EventAlarmMgr(getActivity());
        Set<Long> alarms = alarmMgr.getAlarms(_event.id);
        if (alarms == null) {
            return;
        }

        LinearLayout reminderList = (LinearLayout)getActivity().
            findViewById(R.id.event_reminder_list);

        for (Long alarm : alarms) {
            long ts = alarm.longValue();
            EventAlarm eventAlarm = new EventAlarm(getActivity(), ts, _event.timestamp);
            _eventAlarmAdapter.add(eventAlarm);
            int pos = _eventAlarmAdapter.getPosition(eventAlarm);
            // get view and add it to the reminder list
            View alarmView = _eventAlarmAdapter.getView(pos, null, (ViewGroup)reminderList);
            // disable "delete" button, it should be present only in entry editor
            View button = alarmView.findViewById(R.id.reminder_remove_button);
            button.setVisibility(View.GONE);
            ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            reminderList.addView(alarmView, params);
        }
    }

    // populate UI controls with project data received from Loader
    private void populateProjectDetails() {
        Log.d("[Scriba]", "populateProjectDetails()");

        if (_project == null) {
            // there's no data
            Log.e("[Scriba]", "populateProjectDetails() called with null _project");
            return;
        }

        // verify that project id is the same as requested
        if (!_entryId.equals(_project.id)) {
            Log.e("[Scriba]", "Requested project details for id=" + _entryId.toString() +
                              ", but received details for id=" + _project.id.toString());
            return;
        }

        TextView txt = (TextView)getActivity().findViewById(R.id.project_title_text);
        txt.setText(_project.title);

        txt = (TextView)getActivity().findViewById(R.id.project_descr_text);
        txt.setText(_project.descr);

        // get company data for the project and display company name
        ScribaDBManager.useDB(getActivity());
        Company company = ScribaDB.getCompany(_project.company_id);
        ScribaDBManager.releaseDB();

        if (company != null) {
            txt = (TextView)getActivity().findViewById(R.id.project_company_text);
            txt.setText(company.name);
            View project_company = getActivity().findViewById(R.id.project_company);
            project_company.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ask activity to show company details
                    _activityInterface.onEntryChange(EntryType.COMPANY, _project.company_id);
                }
            });
        }
        
        ProjectStateMapper projectStateMapper = new ProjectStateMapper(getActivity());
        String stateStr = projectStateMapper.getString(_project.state);
        txt = (TextView)getActivity().findViewById(R.id.project_state_text);
        txt.setText(stateStr);

        CurrencyMapper currencyMapper = new CurrencyMapper(getActivity());
        String currencyStr = currencyMapper.getString(_project.currency);

        txt = (TextView)getActivity().findViewById(R.id.project_cost_text);
        txt.setText((new Long(_project.cost)).toString() + " " + currencyStr);

        // start time
        Date date = new Date(_project.start_time * 1000); // convert to ms
        DateFormat format = DateFormat.getDateTimeInstance();
        txt = (TextView)getActivity().findViewById(R.id.project_start_time_text);
        txt.setText(format.format(date));

        // mod time
        date = new Date(_project.mod_time * 1000); // convert to ms
        format = DateFormat.getDateTimeInstance();
        txt = (TextView)getActivity().findViewById(R.id.project_mod_time_text);
        txt.setText(format.format(date));
    }

    // populate UI controls with POC data received from Loader
    private void populatePOCDetails() {
        Log.d("[Scriba]", "populatePOCDetails()");

        if (_poc == null) {
            // there's no data
            Log.e("[Scriba]", "populatePOCDetails() called with null _poc");
            return;
        }

        // verify that POC id is the same as requested
        if (!_entryId.equals(_poc.id)) {
            Log.e("[Scriba]", "Requested poc details for id=" + _entryId.toString() +
                              ", but received details for id=" + _poc.id.toString());
            return;
        }

        TextView txt = (TextView)getActivity().findViewById(R.id.poc_firstname_text);
        txt.setText(_poc.firstname);

        txt = (TextView)getActivity().findViewById(R.id.poc_secondname_text);
        txt.setText(_poc.secondname);

        txt = (TextView)getActivity().findViewById(R.id.poc_lastname_text);
        txt.setText(_poc.lastname);

        txt = (TextView)getActivity().findViewById(R.id.poc_mobilenum_text);
        txt.setText(_poc.mobilenum);
        new PhoneItemHandler(R.id.poc_mobilenum,
                             R.id.poc_mobilenum_button,
                             _poc.mobilenum);

        txt = (TextView)getActivity().findViewById(R.id.poc_phonenum_text);
        txt.setText(_poc.phonenum);
        new PhoneItemHandler(R.id.poc_phonenum,
                             R.id.poc_phonenum_button,
                             _poc.phonenum);

        txt = (TextView)getActivity().findViewById(R.id.poc_email_text);
        txt.setText(_poc.email);

        txt = (TextView)getActivity().findViewById(R.id.poc_position_text);
        txt.setText(_poc.position);

        // get POC's company data and display company name
        ScribaDBManager.useDB(getActivity());
        Company company = ScribaDB.getCompany(_poc.company_id);
        ScribaDBManager.releaseDB();

        if (company != null) {
            txt = (TextView)getActivity().findViewById(R.id.poc_company_text);
            txt.setText(company.name);
            View poc_company = getActivity().findViewById(R.id.poc_company);
            poc_company.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ask activity to show company details
                    _activityInterface.onEntryChange(EntryType.COMPANY, _poc.company_id);
                }
            });
        }
    }
}
