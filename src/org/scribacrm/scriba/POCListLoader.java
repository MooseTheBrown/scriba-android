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

import android.util.Log;
import android.content.AsyncTaskLoader;
import org.scribacrm.libscriba.*;
import android.content.Context;
import java.util.UUID;

public class POCListLoader extends AsyncTaskLoader<DataDescriptor []> {

    public enum FilterType {
        ALL,
        NAME,
        COMPANY,
        POSITION,
        EMAIL
    }

    private FilterType _filterType = FilterType.ALL;
    private String _firstname = null;
    private String _secondname = null;
    private String _lastname = null;
    private String _searchParam = null;
    private UUID _companyId = null;

    public POCListLoader(Context context) {
        super(context);
    }

    // search by name
    public void setNameSearch(String firstname, String secondname, String lastname) {
        _filterType = FilterType.NAME;
        _firstname = firstname;
        _secondname = secondname;
        _lastname = lastname;
    }

    // search by company
    public void setCompanySearch(UUID companyId) {
        _filterType = FilterType.COMPANY;
        _companyId = companyId;
    }

    // search by string according to search type
    public void setSearch(FilterType type, String param) {
        _filterType = type;
        _searchParam = param;
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "POCListLoader.onStartLoading()");
        ScribaDBManager.useDB(getContext());
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "POCListLoader.onReset()");
        ScribaDBManager.releaseDB();
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        Log.d("[Scriba]", "POCListLoader.loadInBackground()");
        Log.d("[Scriba]", "_filterType = " + _filterType);

        DataDescriptor[] result = null;

        switch (_filterType) {
            case ALL:
                result = ScribaDB.getAllPeople();
                break;
            case NAME:
                result = ScribaDB.getPOCByName(_firstname, _secondname, _lastname);
                break;
            case COMPANY:
                result = ScribaDB.getPOCByCompany(_companyId);
                break;
            case POSITION:
                result = ScribaDB.getPOCByPosition(_searchParam);
                break;
            case EMAIL:
                result = ScribaDB.getPOCByEmail(_searchParam);
                break;
        }

        Log.d("[Scriba]", "POCListLoader - loading finished, result length is " + result.length);
        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        // there's no way to cancel loading data from ScribaDB,
        // so do nothing
        Log.d("[Scriba]", "POCListLoader.cancelLoadInBackground()");
    }

    @Override
    protected boolean onCancelLoad() {
        // our task cannot be canceled
        Log.d("[Scriba]", "POCListLoader.onCancelLoad()");
        return false;
    }
}
