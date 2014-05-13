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

import android.content.AsyncTaskLoader;
import org.scribacrm.libscriba.*;
import android.content.Context;
import android.util.Log;

public class EntryDetailsLoader<EntryType> extends AsyncTaskLoader<EntryType> {

    private long _entryId = 0;
    private Class<EntryType> _cls;

    public EntryDetailsLoader(Context context, long entryId, Class<EntryType> cls) {
        super(context);

        _entryId = entryId;
        _cls = cls;

        Log.d("[Scriba]", "Initialized EntryDetailsLoader for id=" + _entryId + ", cls=" + _cls);
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "EntryDetailsLoader.onStartLoading()");
        ScribaDBManager.useDB(getContext());
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "EntryDetailsLoader.onReset()");
        ScribaDBManager.releaseDB();
    }

    @Override
    public EntryType loadInBackground() {
        EntryType result = null;

        Log.d("[Scriba]", "EntryDetailsLoader.loadInBackground()");

        if (_cls == Company.class) {
            result = (EntryType)ScribaDB.getCompany(_entryId);
        }
        else if (_cls == Event.class) {
            result = (EntryType)ScribaDB.getEvent(_entryId);
        }
        else if (_cls == POC.class) {
            result = (EntryType)ScribaDB.getPoc(_entryId);
        }
        else if (_cls == Project.class) {
            result = (EntryType)ScribaDB.getProject(_entryId);
        }

        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        Log.d("[Scriba]", "EntryDetailsLoader.cancelLoadInBackground()");
        // there's no way to cancel loading data from ScribaDB,
        // so do nothing
    }

    @Override
    protected boolean onCancelLoad() {
        Log.d("[Scriba]", "EntryDetailsLoader.onCancelLoad()");
        // this task cannot be canceled
        return false;
    }
}
