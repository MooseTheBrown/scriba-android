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
import android.widget.Toast;
import android.util.Log;
import android.app.Fragment;
import android.widget.ArrayAdapter;
import java.util.UUID;
import java.io.File;
import android.os.Environment;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class EntryListActivity extends Activity
                               implements EntryListFragment.ActivityInterface,
                               ActionBar.OnNavigationListener {

    // request codes for starting different activities
    public static final int REQUEST_ADD_ENTRY = 0;
    public static final int REQUEST_VIEW_ENTRY = 1;

    // supported screen types
    private static final byte SCREEN_TYPE_SMALL = 0;
    private static final byte SCREEN_TYPE_LARGE = 1;

    // fragment tags
    private static final String LIST_FRAG_TAG = "EntryListFragment";
    private static final String DETAILS_VIEW_FRAG_TAG = "EntryDetailsFragment";
    private static final String DETAILS_EDIT_FRAG_TAG = "EntryEditFragment";

    // saved state Bundle keys
    private static final String ENTRY_TYPE_KEY = "EntryType";

    // directory on external storage to store backups to
    private static final String BACKUP_DIR = "/scriba_backup/";

    // type of currently displayed items
    private EntryType _entryType = EntryType.COMPANY;

    // adapter for entry type drop-down list
    private ArrayAdapter<CharSequence> _entryTypeAdapter = null;

    // string values for entry type drop-down list
    private String _entryTypeCompanyStr = null;
    private String _entryTypeEventStr = null;
    private String _entryTypeProjectStr = null;
    private String _entryTypePOCStr = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        _entryTypeAdapter = ArrayAdapter.createFromResource(this,
                            R.array.entry_type_list,
                            android.R.layout.simple_spinner_dropdown_item);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(_entryTypeAdapter, this);

        // load entry type strings
        _entryTypeCompanyStr = getResources().getString(R.string.Companies);
        _entryTypeEventStr = getResources().getString(R.string.Events);
        _entryTypeProjectStr = getResources().getString(R.string.Projects);
        _entryTypePOCStr = getResources().getString(R.string.People);

        // get entry type from saved state
        if (savedInstanceState != null) {
            int entryTypeId = savedInstanceState.getInt(ENTRY_TYPE_KEY, -1);
            if (entryTypeId != -1) {
                setEntryType(entryTypeId);

                // set current entry type as selected in drop-down navigation list
                int pos = -1;
                switch(_entryType) {
                    case COMPANY:
                        pos = _entryTypeAdapter.getPosition(_entryTypeCompanyStr);
                        break;
                    case EVENT:
                        pos = _entryTypeAdapter.getPosition(_entryTypeEventStr);
                        break;
                    case PROJECT:
                        pos = _entryTypeAdapter.getPosition(_entryTypeProjectStr);
                        break;
                    case POC:
                        pos = _entryTypeAdapter.getPosition(_entryTypePOCStr);
                        break;
                }

                getActionBar().setSelectedNavigationItem(pos);
            }
        }

        getActionBar().setTitle(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save entry type
        outState.putInt(ENTRY_TYPE_KEY, _entryType.id());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_list_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_record:
                startAddEntryActivity();
                return true;
            case R.id.action_export_all:
                exportAllRecords();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // EntryListFragment.ActivityInterface implementation
    public void onCompanyClicked(UUID id) {
        onEntryClicked(id);
    }

    public void onEventClicked(UUID id) {
        onEntryClicked(id);
    }

    public void onPOCClicked(UUID id) {
        onEntryClicked(id);
    }

    public void onProjectClicked(UUID id) {
        onEntryClicked(id);
    }

    public EntryType getEntryType() {
        return _entryType;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_ENTRY) {
            if (resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(this, R.string.entry_added, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    // implementation of ActionBar.OnNavigationListener interface
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        EntryType prevType = _entryType;
        String selected = (String)_entryTypeAdapter.getItem(itemPosition);
        if (selected.equals(_entryTypeCompanyStr)) {
            _entryType = EntryType.COMPANY;
        }
        else if (selected.equals(_entryTypeEventStr)) {
            _entryType = EntryType.EVENT;
        }
        else if (selected.equals(_entryTypeProjectStr)) {
            _entryType = EntryType.PROJECT;
        }
        else if (selected.equals(_entryTypePOCStr)) {
            _entryType = EntryType.POC;
        }

        // has entry type changed?
        if (_entryType != prevType) {
            // tell entry list fragment to update contents
            EntryListFragment listFragment = (EntryListFragment)getFragmentManager().
                                             findFragmentByTag(LIST_FRAG_TAG);
            if (listFragment != null) {
                listFragment.changeEntryType(_entryType);
            }
            else {
                Log.e("[Scriba]", "EntryListActivity: could not find EntryListFragment!");
            }
        }

        return true;
    }

    // launch AddEntryActivity
    private void startAddEntryActivity() {
        Intent intent = new Intent(this, AddEntryActivity.class);

        // set entry type
        intent.putExtra(AddEntryActivity.ENTRY_TYPE_INTENT_KEY, _entryType.id());
        startActivityForResult(intent, REQUEST_ADD_ENTRY);
    }

    // launch EntryDetailsActivity
    private void startEntryDetailsActivity(UUID id) {
        Intent intent = new Intent(this, EntryDetailsActivity.class);

        // set entry type
        intent.putExtra(EntryDetailsActivity.ENTRY_TYPE_INTENT_KEY, _entryType.id());
        // set entry id
        intent.putExtra(EntryDetailsActivity.ENTRY_ID_INTENT_KEY, id.toString());
        startActivityForResult(intent, REQUEST_VIEW_ENTRY);
    }

    // determine current screen type
    private byte getScreenType() {
        byte type = SCREEN_TYPE_SMALL;

        Fragment fragment = getFragmentManager().findFragmentByTag(DETAILS_VIEW_FRAG_TAG);
        if (fragment != null) {
            type = SCREEN_TYPE_LARGE;
        }
        else {
            // maybe there's an entry editor fragment
            fragment = getFragmentManager().findFragmentByTag(DETAILS_EDIT_FRAG_TAG);
            if (fragment != null) {
                type = SCREEN_TYPE_LARGE;
            }
        }

        return type;
    }

    // common response to entry click event from EntryListFragment
    private void onEntryClicked(UUID id) {
        if (getScreenType() == SCREEN_TYPE_SMALL) {
            // we're on a small screen device,
            // launch separate activity for entry details
            startEntryDetailsActivity(id);
        }
        else {
            // TODO: large screen device, update entry details fragment
        }
    }

    // set entry type based on its integer representation
    private void setEntryType(int typeId) {
        if (typeId == EntryType.COMPANY.id()) { _entryType = EntryType.COMPANY; }
        else if (typeId == EntryType.EVENT.id()) { _entryType = EntryType.EVENT; }
        else if (typeId == EntryType.POC.id()) { _entryType = EntryType.POC; }
        else if (typeId == EntryType.PROJECT.id()) { _entryType = EntryType.PROJECT; }
    }

    // launch SerializationService to export all data to a file
    private void exportAllRecords() {
        // get descriptors for all entries
        ScribaDBManager.useDB(this);
        DataDescriptor[] companies = ScribaDB.getAllCompanies();
        DataDescriptor[] events = ScribaDB.getAllEvents();
        DataDescriptor[] people = ScribaDB.getAllPeople();
        DataDescriptor[] projects = ScribaDB.getAllProjects();
        ScribaDBManager.releaseDB();

        // create backup file
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // storage not available, display alert
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage(R.string.alert_backup_no_storage);
            alertBuilder.setNeutralButton(R.string.alert_ok,
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog,
                                                                  int id) {
                                                  // do nothing
                                              }
                                          });
            alertBuilder.create().show();
            return;
        }
        File storageDir = Environment.getExternalStorageDirectory();
        String path = storageDir.getAbsolutePath();
        path += BACKUP_DIR;
        // create backup dir if it doesn't exist
        File backupDir = new File(path);
        backupDir.mkdir();
        path += generateBakFileName();

        // save data in background using serialization service
        Intent request = SerializationService.serializeRequest(this,
                             companies,
                             events,
                             people,
                             projects,
                             path);
        startService(request);
    }

    // generate backup file name based on current time
    private String generateBakFileName() {
        String filename = "scriba-";
        // append current timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-kkmmss");
        filename += sdf.format(new Date());
        filename += ".dat";

        return filename;
    }
}
