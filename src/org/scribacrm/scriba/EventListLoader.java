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

public class EventListLoader extends AsyncTaskLoader<DataDescriptor []> {

    private SearchInfo _searchInfo = null;

    public EventListLoader(Context context) {
        super(context);
    }

    public void setSearchInfo(SearchInfo info) {
        _searchInfo = info;
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "EventListLoader.onStartLoading()");
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "EventListLoader.onReset()");
    }

    @Override
    public DataDescriptor[] loadInBackground() {
        ScribaDBManager.useDB(getContext());
        Log.d("[Scriba]", "EventListLoader.loadInBackground()");

        DataDescriptor[] result = null;

        if (_searchInfo == null) {
            // no search info, get all events
            result = ScribaDB.getAllEvents();
        }
        else {
            Log.d("[Scriba]", "search type: " + _searchInfo.searchType());
            switch (_searchInfo.searchType()) {
                case EVENT_DESCR:
                    result = ScribaDB.getEventsByDescr(_searchInfo.stringParam());
                    break;
                case EVENT_COMPANY:
                    result = ScribaDB.getEventsByCompany(_searchInfo.uuidParam());
                    break;
                case EVENT_POC:
                    result = ScribaDB.getEventsByPOC(_searchInfo.uuidParam());
                    break;
                case EVENT_PROJECT:
                    result = ScribaDB.getEventsByProject(_searchInfo.uuidParam());
                    break;
                case EVENT_STATE:
                    result = ScribaDB.getEventsByState(_searchInfo.byteParam());
                    break;
                default:
                    Log.e("[Scriba]", "Unsupported event search type");
                    break;
            }
        }

        if (result != null) {
            result = ScribaDB.fetchAll(result);
            Log.d("[Scriba]", "EventListLoader - loading finished, result length is " +
                  result.length);
        }
        ScribaDBManager.releaseDB();
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
