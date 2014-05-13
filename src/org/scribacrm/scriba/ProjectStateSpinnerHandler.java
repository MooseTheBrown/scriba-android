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
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;
import android.util.Log;

// project state list handler populates spinner with all possible project
// states and reports selected state
public class ProjectStateSpinnerHandler implements AdapterView.OnItemSelectedListener {

    private Context _context = null;
    private byte _state = Project.State.INITIAL;
    // adapter for project state list
    private ArrayAdapter<String> _projectStateListAdapter = null;
    // project state mapper instance
    private ProjectStateMapper _projectStateMapper = null;

    public ProjectStateSpinnerHandler(Context context) {
        _context = context;
        _projectStateMapper = new ProjectStateMapper(_context);
        _projectStateListAdapter = new ArrayAdapter<String>(_context,
            android.R.layout.simple_spinner_item);
        _projectStateListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String stateStr = _projectStateListAdapter.getItem(pos);
        _state = _projectStateMapper.getCode(stateStr);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // reset to default
        _state = Project.State.INITIAL;
    }

    // populate spinner with state strings
    public void populateSpinner(Spinner spinner) {
        populateSpinner(spinner, Project.State.INITIAL);
    }

    // populate spinner with state strings and set selected state
    public void populateSpinner(Spinner spinner, byte selectedState) {
        spinner.setAdapter(_projectStateListAdapter);
        spinner.setOnItemSelectedListener(this);
        String selectedStr = _projectStateMapper.getString(selectedState);

        _projectStateListAdapter.clear();
        String[] strs = _projectStateMapper.getStrings();
        for (String str : strs) {
            _projectStateListAdapter.add(str);
            if (str.equals(selectedStr)) {
                int pos = _projectStateListAdapter.getPosition(str);
                spinner.setSelection(pos);
            }
        }
    }

    // get currently selected state
    public byte getSelectedState() {
        return _state;
    }
}
