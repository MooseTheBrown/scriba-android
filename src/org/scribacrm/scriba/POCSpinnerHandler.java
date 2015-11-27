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
import android.content.Context;
import android.widget.ArrayAdapter;
import android.content.Loader;
import android.app.LoaderManager;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import java.util.UUID;

// POC spinner handler loads list of people using loader, populates
// spinner widget and reports selected POC id
public class POCSpinnerHandler implements
                               LoaderManager.LoaderCallbacks<DataDescriptor[]>,
                               AdapterView.OnItemSelectedListener {

    private Context _context = null;
    private LoaderManager _loaderManager = null;
    // selected POC id
    private UUID _selectedPOCId = null;
    // company id used for POC filter
    private UUID _companyId = null;
    // POC list adapter
    private ArrayAdapter<DataDescriptor> _pocListAdapter = null;
    private Spinner _spinner = null;

    public POCSpinnerHandler(Context context, LoaderManager loaderManager) {
        _context = context;
        _loaderManager = loaderManager;

        _pocListAdapter = new ArrayAdapter<DataDescriptor>(_context,
            android.R.layout.simple_spinner_item);
        _pocListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public Loader<DataDescriptor[]> onCreateLoader(int id, Bundle args) {
        if (id == EntryType.POC.loaderId()) {
            POCListLoader loader = new POCListLoader(_context);
            // if there's a company id set, get people for selected
            // company only
            if (_companyId != null) {
                loader.setSearchInfo(new SearchInfo(SearchInfo.SearchType.POC_COMPANY,
                                                    _companyId));
            }

            return (Loader<DataDescriptor[]>) loader;
        }
        else {
            Log.e("[Scriba]", "POCSpinnerHandler.onCreateLoader() invalid loader id!");
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<DataDescriptor[]> loader, DataDescriptor[] data) {
        _pocListAdapter.clear();
        if (data != null) {
            for (DataDescriptor item : data) {
                _pocListAdapter.add(item);
                if (item.id.equals(_selectedPOCId)) {
                    int pos = _pocListAdapter.getPosition(item);
                    _spinner.setSelection(pos);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<DataDescriptor[]> loader) {
        _pocListAdapter.clear();
    }

    // load POC list from DB
    public void load(Spinner spinner) {
        load(spinner, null, null);
    }

    // load POC list from DB with company filter
    public void load(Spinner spinner, UUID companyId) {
        load(spinner, companyId, null);
    }

    // load POC list from DB with company filter and set selection
    public void load(Spinner spinner, UUID companyId, UUID selectedId) {
        _companyId = companyId;
        _selectedPOCId = selectedId;
        spinner.setAdapter(_pocListAdapter);
        spinner.setOnItemSelectedListener(this);
        _spinner = spinner;
        _loaderManager.restartLoader(EntryType.POC.loaderId(), null, this);
    }

    // AdapterView.onItemSelectedListener implementation
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        DataDescriptor descr = _pocListAdapter.getItem(pos);
        _selectedPOCId = descr.id;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        _selectedPOCId = null;
    }

    // get id of currently selected person
    public UUID getSelectedPOCId() {
        return _selectedPOCId;
    }
}
