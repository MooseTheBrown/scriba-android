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

    private SearchInfo _searchInfo = null;

    public POCListLoader(Context context) {
        super(context);
    }

    public void setSearchInfo(SearchInfo info) {
        _searchInfo = info;
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

        DataDescriptor[] result = null;

        if (_searchInfo == null) {
            // no search info, get all people
            result = ScribaDB.getAllPeople();
        }
        else {
            Log.d("[Scriba]", "search type: " + _searchInfo.searchType());

            switch (_searchInfo.searchType()) {
                case POC_NAME:
                    String[] params = _searchInfo.stringParams();
                    String firstname = null;
                    String secondname = null;
                    String lastname = null;

                    if (params.length > 0) {
                        lastname = params[0];
                    }
                    if (params.length > 1) {
                        firstname = params[1];
                    }
                    if (params.length > 2) {
                        secondname = params[2];
                    }

                    result = ScribaDB.getPOCByName(firstname, secondname, lastname);
                    break;
                case POC_COMPANY:
                    result = ScribaDB.getPOCByCompany(_searchInfo.uuidParam());
                    break;
                case POC_POSITION:
                    result = ScribaDB.getPOCByPosition(_searchInfo.stringParam());
                    break;
                case POC_EMAIL:
                    result = ScribaDB.getPOCByEmail(_searchInfo.stringParam());
                    break;
                case POC_GENERIC:
                    result = genericSearch(_searchInfo.stringParam());
                    break;
                default:
                    Log.e("[Scriba]", "unsupported POC search type");
                    break;
            }
        }

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

    private DataDescriptor[] genericSearch(String param) {
        if (param == null) {
            return null;
        }

        DataDescriptor[] firstnameResults = ScribaDB.getPOCByName(param, null, null);
        DataDescriptor[] secondnameResults = ScribaDB.getPOCByName(null, param, null);
        DataDescriptor[] lastnameResults = ScribaDB.getPOCByName(null, null, param);
        DataDescriptor[] positionResults = ScribaDB.getPOCByPosition(param);
        DataDescriptor[] emailResults = ScribaDB.getPOCByEmail(param);

        int resultLength = firstnameResults.length + secondnameResults.length +
                            lastnameResults.length + positionResults.length +
                            emailResults.length;
        DataDescriptor[] results = new DataDescriptor[resultLength];
        int i = 0;
        for (DataDescriptor entry : firstnameResults) {
            results[i++] = entry;
        }
        for (DataDescriptor entry : secondnameResults) {
            results[i++] = entry;
        }
        for (DataDescriptor entry : lastnameResults) {
            results[i++] = entry;
        }
        for (DataDescriptor entry : positionResults) {
            results[i++] = entry;
        }
        for (DataDescriptor entry : emailResults) {
            results[i++] = entry;
        }

        return results;
    }
}
