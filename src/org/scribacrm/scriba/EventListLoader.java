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

public class EventListLoader extends AsyncTaskLoader<DataDescriptor []> {

    public enum FilterType {
        ALL,
        COMPANY,
        POC,
        PROJECT
    }

    private FilterType _filterType = FilterType.ALL;
    private UUID _searchId = null;

    public EventListLoader(Context context) {
        super(context);
    }

    // search by company
    public void setCompanySearch(UUID companyId) {
        _filterType = FilterType.COMPANY;
        _searchId = companyId;
    }

    // search by POC
    public void setPOCSearch(UUID pocId) {
        _filterType = FilterType.POC;
        _searchId = pocId;
    }

    // search by project
    public void setProjectSearch(UUID projectId) {
        _filterType = FilterType.PROJECT;
        _searchId = projectId;
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "EventListLoader.onStartLoading()");
        ScribaDBManager.useDB(getContext());
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "EventListLoader.onReset()");
        ScribaDBManager.releaseDB();
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        Log.d("[Scriba]", "EventListLoader.loadInBackground()");
        Log.d("[Scriba]", "_filterType = " + _filterType);

        DataDescriptor[] result = null;

        switch (_filterType) {
            case ALL:
                result = ScribaDB.getAllEvents();
                break;
            case COMPANY:
                result = ScribaDB.getEventsByCompany(_searchId);
                break;
            case POC:
                result = ScribaDB.getEventsByPOC(_searchId);
                break;
            case PROJECT:
                result = ScribaDB.getEventsByProject(_searchId);
                break;
        }

        Log.d("[Scriba]", "EventListLoader - loading finished, result length is " + result.length);
        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        // there's no way to cancel loading data from ScribaDB,
        // so do nothing
        Log.d("[Scriba]", "EventListLoader.cancelLoadInBackground()");
    }

    @Override
    protected boolean onCancelLoad() {
        // our task cannot be canceled
        Log.d("[Scriba]", "EventListLoader.onCancelLoad()");
        return false;
    }
}
