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
import android.util.Log;
import android.app.ListFragment;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Loader;
import android.widget.ListView;
import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import java.util.UUID;
import android.widget.TextView;

public class EntryListFragment extends ListFragment
                               implements LoaderManager.LoaderCallbacks<DataDescriptor[]>,
                               AbsListView.MultiChoiceModeListener {

    public interface ActivityInterface {
        void onCompanyClicked(UUID id);
        void onEventClicked(UUID id);
        void onPOCClicked(UUID id);
        void onProjectClicked(UUID id);
        EntryType getEntryType();
        SearchInfo getSearchInfo();
    }
    
    // type of currently displayed items
    private EntryType _entryType = EntryType.COMPANY;
    // adapter for ListView
    private EntryListAdapter _adapter = null;
    // adapter for event ListView
    private EventListAdapter _eventAdapter = null;
    // activity handling entry clicks
    private ActivityInterface _activityInterface = null;
    // project state filter dialog instance
    ProjStateFilterDialog _projStateFilterDialog = null;
    // event state filter dialog instance
    EventStateFilterDialog _eventStateFilterDialog = null;

    public EntryListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d("[Scriba]", "EntryListFragment.onAttach()");
        super.onAttach(activity);
        _activityInterface = (ActivityInterface)activity;
        _adapter = new EntryListAdapter(activity.getLayoutInflater());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.entry_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _entryType = _activityInterface.getEntryType();
        Log.d("[Scriba]", "EntryListFragment.onActivityCreated(), _entryType=" + _entryType);

        if (_entryType == EntryType.EVENT) {
            // events require different adapter
            _eventAdapter = new EventListAdapter(getActivity(),
                                                 getActivity().getLayoutInflater());
            setListAdapter((ListAdapter)_eventAdapter);
        }
        else {
            setListAdapter((ListAdapter)_adapter);
        }

        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        loadData(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.entry_list_frag_actions, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        MenuItem resetFilterItem = menu.findItem(R.id.action_reset_filter);
        if (((_entryType == EntryType.PROJECT) ||
            (_entryType == EntryType.EVENT)) &&
            (_activityInterface.getSearchInfo() == null)) {
            filterItem.setVisible(true);
            resetFilterItem.setVisible(true);
        }
        else {
            filterItem.setVisible(false);
            resetFilterItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                if (_entryType == EntryType.PROJECT) {
                    // project filter
                    byte currState = Project.State.INITIAL;
                    if (_projStateFilterDialog != null) {
                        currState = _projStateFilterDialog.getProjectState();
                    }
                    ProjStateFilterDialog.DismissListener listener =
                        new ProjStateFilterDialog.DismissListener() {
                            public void onDismiss() {
                                // force data reload when new filter type is
                                // selected by user
                                loadData(true);
                            }
                        };
                    _projStateFilterDialog = new ProjStateFilterDialog(currState, listener);
                    _projStateFilterDialog.show(getActivity().getFragmentManager(),
                                                "projFilterDialog");
                }
                else {
                    // event filter
                    byte currState = Event.State.SCHEDULED;
                    if (_eventStateFilterDialog != null) {
                        currState = _eventStateFilterDialog.getEventState();
                    }
                    EventStateFilterDialog.DismissListener listener = 
                        new EventStateFilterDialog.DismissListener() {
                            public void onDismiss() {
                                loadData(true);
                            }
                        };
                    _eventStateFilterDialog = new EventStateFilterDialog(currState, listener);
                    _eventStateFilterDialog.show(getActivity().getFragmentManager(),
                                                 "evtFilterDialog");
                }
                break;
            case R.id.action_reset_filter:
                // reset filter and force data reload
                if (_entryType == EntryType.PROJECT) {
                    _projStateFilterDialog = null;
                }
                else {
                    _eventStateFilterDialog = null;
                }
                loadData(true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // let activity handle the entry click
        DataDescriptor entry;
        switch (_entryType) {
            case COMPANY:
                entry = _adapter.getItem(position);
                _activityInterface.onCompanyClicked(entry.id);
                break;
            case EVENT:
                entry = _eventAdapter.getItem(position);
                _activityInterface.onEventClicked(entry.id);
                break;
            case POC:
                entry = _adapter.getItem(position);
                _activityInterface.onPOCClicked(entry.id);
                break;
            case PROJECT:
                entry = _adapter.getItem(position);
                _activityInterface.onProjectClicked(entry.id);
                break;
        }
    }

    @Override
    public Loader<DataDescriptor[]> onCreateLoader(int id, Bundle args) {
        Log.d("[Scriba]", "EntryListFragment.onCreateLoader(), id = " + id);

        Loader<DataDescriptor[]> loader = null;
        SearchInfo searchInfo = _activityInterface.getSearchInfo();

        if (id == EntryType.COMPANY.loaderId()) {
            CompanyListLoader compLoader = new CompanyListLoader(getActivity());
            if (searchInfo != null) {
                compLoader.setSearchInfo(searchInfo);
            }
            loader = (Loader<DataDescriptor[]>)compLoader;
        }
        else if (id == EntryType.EVENT.loaderId()) {
            EventListLoader eventLoader = new EventListLoader(getActivity());
            if (searchInfo != null) {
                eventLoader.setSearchInfo(searchInfo);
            }
            else if (_eventStateFilterDialog != null) {
                eventLoader.setSearchInfo(new SearchInfo(SearchInfo.SearchType.EVENT_STATE,
                    _eventStateFilterDialog.getEventState()));
            }
            loader = (Loader<DataDescriptor[]>)eventLoader;
        }
        else if (id == EntryType.POC.loaderId()) {
            POCListLoader pocLoader = new POCListLoader(getActivity());
            if (searchInfo != null) {
                pocLoader.setSearchInfo(searchInfo);
            }
            loader = (Loader<DataDescriptor[]>)pocLoader;
        }
        else if (id == EntryType.PROJECT.loaderId()) {
            ProjectListLoader projLoader = new ProjectListLoader(getActivity());
            if (searchInfo != null) {
                projLoader.setSearchInfo(searchInfo);
            }
            else if (_projStateFilterDialog != null) {
                projLoader.setSearchInfo(new SearchInfo(SearchInfo.SearchType.PROJECT_STATE,
                                                        _projStateFilterDialog.getProjectState()));
            }
            loader = (Loader<DataDescriptor[]>)projLoader;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<DataDescriptor[]> loader, DataDescriptor[] data) {
        Log.d("[Scriba]", "EntryListFragment.onLoadFinished()");
        if (data != null) {
            Log.d("[Scriba]", "data length is " + data.length);
        }

        EntryListAdapter adapter = _adapter;
        // different adapter is used for event list
        if (_entryType == EntryType.EVENT) {
            adapter = (EntryListAdapter)_eventAdapter;
        }

        // populate ListView adapter with data
        adapter.clear();
        if (data != null) {
            for (DataDescriptor item : data) {
                adapter.add(item);
            }
        }

        TextView textView = (TextView)getActivity().findViewById(android.R.id.empty);
        if (textView != null) {
            textView.setText(R.string.no_records);
        }
    }

    @Override
    public void onLoaderReset(Loader<DataDescriptor[]> loader) {
        Log.d("[Scriba]", "EntryListFragment.onLoaderReset()");
        _adapter.clear();
        if (_eventAdapter != null) {
            _eventAdapter.clear();
        }
    }

    // AbsListView.MultiChoiceModeListener implementation
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
        Log.d("[Scriba]", "EntryListFragment.onItemCheckedStateChanged(), position=" + position +
              ", checked=" + checked);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.entry_list_context, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_records:
                deleteSelectedEntries();
                mode.finish();
                // after some items are removed, force list refresh
                loadData(true);
                return true;
            default:
                return false;
        }
    }

    // change type of entries displayed by fragment
    public void changeEntryType(EntryType newType) {
        Log.d("[Scriba]", "EntryListFragment.changeEntryType(), newType = " + newType);

        // destroy loader for old type of entries
        getLoaderManager().destroyLoader(_entryType.loaderId());

        if (newType == EntryType.EVENT) {
            // switching to event view
            if (_eventAdapter == null) {
                _eventAdapter = new EventListAdapter(getActivity(),
                                                     getActivity().getLayoutInflater());
            }

            setListAdapter((ListAdapter)_eventAdapter);
        }
        else if (_entryType == EntryType.EVENT) {
            // switching from event view
            setListAdapter((ListAdapter)_adapter);
        }

        _entryType = newType;

        // force data loading for new type of entries
        loadData(true);
    }

    // called by activity when user enters new search query
    public void onNewSearch() {
        // force data reload using new search query
        loadData(true);
    }

    // initiate data loading in background using appropriate loader
    private void loadData(boolean forceReload) {
        Log.d("[Scriba]", "EntryListFragment.loadData(" + forceReload + ")");
        TextView textView = (TextView)getActivity().findViewById(android.R.id.empty);
        if (textView != null) {
            textView.setText(R.string.loading_data);
        }

        if (forceReload == true) {
            getLoaderManager().restartLoader(_entryType.loaderId(), null, this);
        }
        else {
            getLoaderManager().initLoader(_entryType.loaderId(), null, this);
        }
    }

    private void deleteSelectedEntries() {
        Log.d("[Scriba]", "EntryListFragment.deleteSelectedEntries()");

        EntryListAdapter adapter = _adapter;
        if (_entryType == EntryType.EVENT) {
            // events use different adapter
            adapter = (EntryListAdapter)_eventAdapter;
        }

        ScribaDBManager.useDB(getActivity());
        SparseBooleanArray checked_item_pos = getListView().getCheckedItemPositions();
        Log.d("[Scriba]", "number of items: " + checked_item_pos.size());
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked_item_pos.get(i) == true) {
                Log.d("[Scriba]", "Removing item at position " + i);
                DataDescriptor entry = adapter.getItem(i);
                switch (_entryType) {
                    case COMPANY:
                        ScribaDB.removeCompany(entry.id);
                        break;
                    case EVENT:
                        ScribaDB.removeEvent(entry.id);
                        break;
                    case POC:
                        ScribaDB.removePOC(entry.id);
                        break;
                    case PROJECT:
                        ScribaDB.removeProject(entry.id);
                        break;
                }
            }
        }
        ScribaDBManager.releaseDB();
    }
}
