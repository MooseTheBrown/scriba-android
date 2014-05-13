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

public class CompanyListLoader extends AsyncTaskLoader<DataDescriptor []> {

    public enum FilterType {
        ALL,
        NAME,
        JUR_NAME,
        ADDRESS
    }

    private FilterType _filterType = FilterType.ALL;
    private String _searchParam = null;

    public CompanyListLoader(Context context) {
        super(context);
    }

    public void setSearchFilter(FilterType type, String param) {
        _filterType = type;
        _searchParam = param;
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "CompanyListLoader.onStartLoading()");
        ScribaDBManager.useDB(getContext());
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "CompanyListLoader.onReset()");
        ScribaDBManager.releaseDB();
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        Log.d("[Scriba]", "CompanyListLoader.loadInBackground()");
        Log.d("[Scriba]", "_filterType = " + _filterType);

        DataDescriptor[] result = null;

        switch (_filterType) {
            case ALL:
                result = ScribaDB.getAllCompanies();
                break;
            case NAME:
                result = ScribaDB.getCompaniesByName(_searchParam);
                break;
            case JUR_NAME:
                result = ScribaDB.getCompaniesByJurName(_searchParam);
                break;
            case ADDRESS:
                result = ScribaDB.getCompaniesByAddress(_searchParam);
                break;
        }

        Log.d("[Scriba]", "CompanyListLoader - loading finished, result length is " + result.length);
        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        // there's no way to cancel loading data from ScribaDB,
        // so do nothing
        Log.d("[Scriba]", "CompanyListLoader.cancelLoadInBackground()");
    }

    @Override
    protected boolean onCancelLoad() {
        // our task cannot be canceled
        Log.d("[Scriba]", "CompanyListLoader.onCancelLoad()");
        return false;
    }
}
