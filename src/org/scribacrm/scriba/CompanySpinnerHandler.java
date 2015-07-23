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

// company spinner handler loads company list using loader, populates given
// spinner widget and reports selected company id
public class CompanySpinnerHandler implements
                                   LoaderManager.LoaderCallbacks<DataDescriptor[]>,
                                   AdapterView.OnItemSelectedListener {

    public interface OnSelectedListener {
        void onCompanySelected(UUID id);
    }

    private Context _context = null;
    private LoaderManager _loaderManager = null;
    Spinner _spinner = null;
    // currently selected company
    private UUID _companyId = null;
    // company list adapter
    private ArrayAdapter<DataDescriptor> _companyListAdapter = null;
    private OnSelectedListener _listener = null;

    public CompanySpinnerHandler(Context context,
                                 LoaderManager loaderManager,
                                 OnSelectedListener listener) {
        _context = context;
        _listener = listener;
        _loaderManager = loaderManager;

        // initialize company list adapter
        _companyListAdapter = new ArrayAdapter<DataDescriptor>(_context,
            android.R.layout.simple_spinner_item);
        _companyListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public Loader<DataDescriptor[]> onCreateLoader(int id, Bundle args) {
        if (id == EntryType.COMPANY.loaderId()) {
            return (Loader<DataDescriptor[]>)(new CompanyListLoader(_context));
        }
        else {
            Log.e("[Scriba]", "CompanySpinnerHandler.onCreateLoader() invalid loader id!");
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<DataDescriptor[]> loader, DataDescriptor[] data) {
        _companyListAdapter.clear();
        DataDescriptor selectedItem = null;
        if (data != null) {
            for (DataDescriptor item : data) {
                _companyListAdapter.add(item);
                if (item.id.equals(_companyId)) {
                    // remember company, which has to be selected
                    selectedItem = item;
                }
            }
        }

        if (selectedItem != null) {
            int pos = _companyListAdapter.getPosition(selectedItem);
            _spinner.setSelection(pos);
        }
        else {
            if (data != null) {
                _companyId = data[0].id;
            }
            else {
                // there are no companies at all
                _companyId = null;
            }
        }

        // notify listener about selection
        if (_listener != null) {
            _listener.onCompanySelected(_companyId);
        }
    }

    @Override
    public void onLoaderReset(Loader<DataDescriptor[]> loader) {
        _companyListAdapter.clear();
    }

    // load company list from DB into spinner widget
    public void load(Spinner spinner) {
        load(spinner, null);
    }

    // load company list from DB into spinner widget and set selection
    // to company with selectedId
    public void load(Spinner spinner, UUID selectedId) {
        _companyId = selectedId;
        _spinner = spinner;
        spinner.setAdapter(_companyListAdapter);
        spinner.setOnItemSelectedListener(this);
        _loaderManager.restartLoader(EntryType.COMPANY.loaderId(), null, this);
    }

    // AdapterView.onItemSelectedListener implementation
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        UUID oldId = _companyId;
        DataDescriptor descr = _companyListAdapter.getItem(pos);
        _companyId = descr.id;
        // only notify listener if selection has changed
        if ((!oldId.equals(_companyId)) && (_listener != null)) {
            _listener.onCompanySelected(_companyId);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        _companyId = null;
    }

    // get id of currently selected company
    public UUID getSelectedCompanyId() {
        return _companyId;
    }
}
