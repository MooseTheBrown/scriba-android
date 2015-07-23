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
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;

// Currency spinner handler populates spinner with available currency types
// and reports type selected by user
public class CurrencySpinnerHandler implements AdapterView.OnItemSelectedListener {

    private Context _context = null;
    private byte _currency = Project.Currency.RUB;
    // adapter for currency list
    private ArrayAdapter<String> _currencyListAdapter = null;
    // currency mapper instance
    private CurrencyMapper _currencyMapper = null;

    public CurrencySpinnerHandler(Context context) {
        _context = context;
        _currencyMapper = new CurrencyMapper(_context);
        _currencyListAdapter = new ArrayAdapter<String>(_context,
            android.R.layout.simple_spinner_item);
        _currencyListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String currencyStr = _currencyListAdapter.getItem(pos);
        _currency = _currencyMapper.getCode(currencyStr);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // reset to default
        _currency = Project.Currency.RUB;
    }

    // populate spinner with currency strings
    public void populateSpinner(Spinner spinner) {
        populateSpinner(spinner, Project.Currency.RUB);
    }

    // populate spinner with currency strings and set selected currency 
    public void populateSpinner(Spinner spinner, byte selectedCurrency) {
        spinner.setAdapter(_currencyListAdapter);
        spinner.setOnItemSelectedListener(this);
        String selectedStr = _currencyMapper.getString(selectedCurrency);

        _currencyListAdapter.clear();
        String[] strs = _currencyMapper.getStrings();
        for (String str : strs) {
            _currencyListAdapter.add(str);
            if (str.equals(selectedStr)) {
                int pos = _currencyListAdapter.getPosition(str);
                spinner.setSelection(pos);
            }
        }
    }

    // get currently selected currency
    public byte getSelectedCurrency() {
        return _currency;
    }
}
