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

    private SearchInfo _searchInfo = null;

    public CompanyListLoader(Context context) {
        super(context);
    }

    public void setSearchInfo(SearchInfo info) {
        _searchInfo = info;
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

        DataDescriptor[] result = null;

        if (_searchInfo == null) {
            // retrieve all companies
            result = ScribaDB.getAllCompanies();
        }
        else {
            Log.d("[Scriba]", "search type: " + _searchInfo.searchType());
            switch (_searchInfo.searchType()) {
                case COMPANY_NAME:
                    String name = _searchInfo.stringParam();
                    result = ScribaDB.getCompaniesByName(name);
                    break;
                case COMPANY_JUR_NAME:
                    String jur_name = _searchInfo.stringParam();
                    result = ScribaDB.getCompaniesByJurName(jur_name);
                    break;
                case COMPANY_ADDRESS:
                    String addr = _searchInfo.stringParam();
                    result = ScribaDB.getCompaniesByAddress(addr);
                    break;
                case COMPANY_GENERIC:
                    result = genericSearch(_searchInfo.stringParam());
                    break;
                default:
                    Log.e("[Scriba]", "Unsupported company search type");
                    break;
            }
        }

        if (result != null) {
            Log.d("[Scriba]", "CompanyListLoader - loading finished, result length is " +
                  result.length);
        }
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

    // perform generic company search by given String parameter
    private DataDescriptor[] genericSearch(String param) {
        if (param == null) {
            Log.e("[Scriba]", "attempted generic search with null parameter");
            return null;
        }

        DataDescriptor[] nameResults = ScribaDB.getCompaniesByName(param);
        DataDescriptor[] jurNameResults = ScribaDB.getCompaniesByJurName(param);
        int totalLength = nameResults.length + jurNameResults.length;
        DataDescriptor[] result = new DataDescriptor[totalLength];
        for (int i = 0; i < nameResults.length; i++) {
            result[i] = nameResults[i];
        }
        for (int i = 0; i < jurNameResults.length; i++) {
            result[i + nameResults.length] = jurNameResults[i];
        }

        return result;
    }
}
