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
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "POCListLoader.onReset()");
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        ScribaDBManager.useDB(getContext());
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
                    result = ScribaDB.getPOCByName(_searchInfo.stringParam());
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
                default:
                    Log.e("[Scriba]", "unsupported POC search type");
                    break;
            }
        }

        if (result != null) {
            Log.d("[Scriba]", "POCListLoader - loading finished, result length is " +
                  result.length);
            result = ScribaDB.fetchAll(result);
        }

        ScribaDBManager.releaseDB();
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
