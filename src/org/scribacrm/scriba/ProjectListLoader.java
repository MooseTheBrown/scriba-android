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

public class ProjectListLoader extends AsyncTaskLoader<DataDescriptor []> {

    private SearchInfo _searchInfo = null;

    public ProjectListLoader(Context context) {
        super(context);
    }

    public void setSearchInfo(SearchInfo info) {
        _searchInfo = info;
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "ProjectListLoader.onStartLoading()");
        ScribaDBManager.useDB(getContext());
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "ProjectListLoader.onReset()");
        ScribaDBManager.releaseDB();
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        Log.d("[Scriba]", "ProjectListLoader.loadInBackground()");

        DataDescriptor[] result = null;

        if (_searchInfo == null) {
            // no search info, get all projects
            result = ScribaDB.getAllProjects();
        }
        else {
            Log.d("[Scriba]", "search type: " + _searchInfo.searchType());

            switch (_searchInfo.searchType()) {
                case PROJECT_TITLE:
                    result = ScribaDB.getProjectsByTitle(_searchInfo.stringParam());
                    break;
                case PROJECT_COMPANY:
                    result = ScribaDB.getProjectsByCompany(_searchInfo.uuidParam());
                    break;
                case PROJECT_STATE:
                    result = ScribaDB.getProjectsByState(_searchInfo.byteParam());
                    break;
                default:
                    Log.e("[Scriba]", "Unsupported project search type");
                    break;
            }
        }

        if (result != null) {
            Log.d("[Scriba]", "ProjectListLoader - loading finished, result length is " +
                  result.length);
        }
        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        // there's no way to cancel loading data from ScribaDB,
        // so do nothing
        Log.d("[Scriba]", "ProjectListLoader.cancelLoadInBackground()");
    }

    @Override
    protected boolean onCancelLoad() {
        // our task cannot be canceled
        Log.d("[Scriba]", "ProjectListLoader.onCancelLoad()");
        return false;
    }
}
