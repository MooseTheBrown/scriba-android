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
import android.content.Intent;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.util.Log;

public class EntryDetailsActivity extends Activity
                                  implements EntryDetailsFragment.EntryDetailsActivityInterface {

    // keys for Intents and Bundles
    public static final String ENTRY_TYPE_INTENT_KEY = "EntryType";
    public static final String ENTRY_ID_INTENT_KEY = "EntryId";

    // activity type: view or edit
    private static final byte ACTIVITY_TYPE_VIEW = 0;
    private static final byte ACTIVITY_TYPE_EDIT = 1;

    // fragment tags
    private static final String VIEW_FRAGMENT_TAG = "ViewFragment";
    private static final String EDIT_FRAGMENT_TAG = "EditFragment";

    // type and id of currently displayed entry
    private EntryType _entryType = EntryType.COMPANY;
    private long _entryId = 0; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // try to get entry type and id from saved state first
        if (savedInstanceState != null) {
            int typeCode = savedInstanceState.getInt(ENTRY_TYPE_INTENT_KEY, -1);
            if (typeCode != -1) {
                setEntryType(typeCode);
                _entryId = savedInstanceState.getLong(ENTRY_ID_INTENT_KEY, 0);
            }
        }
        else {
            // nothing in saved state, get entry type and id from intent
            Intent intent = getIntent();

            // get entry type
            setEntryType(intent.getIntExtra(ENTRY_TYPE_INTENT_KEY, EntryType.COMPANY.id()));
            // get entry id
            _entryId = intent.getLongExtra(ENTRY_ID_INTENT_KEY, 0);
        }

        setContentView(R.layout.entry_details);

        // add view fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = (Fragment)new EntryDetailsFragment();
        transaction.add(R.id.entry_details_container, fragment, VIEW_FRAGMENT_TAG);
        transaction.commit();

        setActionBarTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (getActivityType() == ACTIVITY_TYPE_VIEW) {
            // view mode menu
            inflater.inflate(R.menu.entry_details_actions, menu);
        }
        else {
            // edit mode menu
            inflater.inflate(R.menu.add_entry_actions, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEdit();
                return true;
            case R.id.action_save:
                onSave();
                return true;
            case R.id.action_cancel:
                onCancel();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save current entry type and id
        outState.putInt(ENTRY_TYPE_INTENT_KEY, _entryType.id());
        outState.putLong(ENTRY_ID_INTENT_KEY, _entryId);
    }

    // EntryDetailsActivityInterface implementation
    public EntryType getEntryType() {
        return _entryType;
    }

    public long getEntryId() {
        return _entryId;
    }

    public void onFragmentResumed(EntryType type, long id) {
        Log.d("[Scriba]", "EntryDetailsActivity.onFragmentResumed, type=" + type +
              ", id=" + id);
        // a framgment has been resumed
        // bring entry type and id in sync with currently displayed fragment
        _entryType = type;
        _entryId = id;
        setActionBarTitle();
    }

    // this is called by EntryDetailsFragment each time new entry
    // has to be displayed
    public void onEntryChange(EntryType newType, long newId) {
        Log.d("[Scriba]", "EntryDetailsActivity.onEntryChange(), type=" + newType +
              ", id=" + newId);

        if (getActivityType() == ACTIVITY_TYPE_EDIT) {
            // this should not happen
            Log.e("[Scriba]", "EntryDetailsActivity.onEntryChange() called from edit fragment");
            return;
        }

        _entryType = newType;
        _entryId = newId;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment viewFragment = (Fragment)new EntryDetailsFragment();
        transaction.replace(R.id.entry_details_container, viewFragment, VIEW_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();

        setActionBarTitle();
    }

    // edit menu item clicked
    private void onEdit() {
        Fragment editFragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        EntryDetailsFragment viewFragment = (EntryDetailsFragment)fragmentManager.
                                            findFragmentByTag(VIEW_FRAGMENT_TAG);

        // fetch entry data from view fragment and show edit fragment
        switch (_entryType) {
            case COMPANY:
                editFragment = (Fragment)new EditEntryFragment(viewFragment.getCompany());
                break;
            case EVENT:
                editFragment = (Fragment)new EditEntryFragment(viewFragment.getEvent());
                break;
            case PROJECT:
                editFragment = (Fragment)new EditEntryFragment(viewFragment.getProject());
                break;
            case POC:
                editFragment = (Fragment)new EditEntryFragment(viewFragment.getPOC());
                break;
            default:
                break;
        }
            
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.entry_details_container, editFragment, EDIT_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // save menu item clicked
    private void onSave() {
        FragmentManager fragmentManager = getFragmentManager();
        // save user modifications
        EditEntryFragment editFragment = (EditEntryFragment)fragmentManager.
                                         findFragmentByTag(EDIT_FRAGMENT_TAG);
        editFragment.save();
        Toast toast = Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT);
        toast.show();

        // replace edit fragment with view fragment
        fragmentManager.popBackStackImmediate();
    }

    // cancel menu item clicked
    private void onCancel() {
        // replace edit fragment with view fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate();
    }

    // set entry type based on its integer representation
    private void setEntryType(int typeId) {
        if (typeId == EntryType.COMPANY.id()) { _entryType = EntryType.COMPANY; }
        else if (typeId == EntryType.EVENT.id()) { _entryType = EntryType.EVENT; }
        else if (typeId == EntryType.POC.id()) { _entryType = EntryType.POC; }
        else if (typeId == EntryType.PROJECT.id()) { _entryType = EntryType.PROJECT; }
    }

    private byte getActivityType() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.findFragmentByTag(EDIT_FRAGMENT_TAG) != null) {
            Log.d("[Scriba]", "EntryDetailsActivity.getActivityType(), type is EDIT");
            return ACTIVITY_TYPE_EDIT;
        }
        else {
            Log.d("[Scriba]", "EntryDetailsActivity.getActivityType(), type is VIEW");
            return ACTIVITY_TYPE_VIEW;
        }
    }

    // set action bar title according to current entry type
    private void setActionBarTitle() {
        String title = null;

        switch (_entryType) {
            case COMPANY:
                title = getResources().getString(R.string.Company);
                break;
            case EVENT:
                title = getResources().getString(R.string.Event);
                break;
            case PROJECT:
                title = getResources().getString(R.string.Project);
                break;
            case POC:
                title = getResources().getString(R.string.POC);
                break;
        }

        if (title != null) {
            getActionBar().setTitle(title);
        }
    }
}
