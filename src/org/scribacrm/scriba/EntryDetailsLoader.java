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

import android.content.AsyncTaskLoader;
import org.scribacrm.libscriba.*;
import android.content.Context;
import android.util.Log;
import java.util.UUID;

public class EntryDetailsLoader<EntryType> extends AsyncTaskLoader<EntryType> {

    private UUID _entryId = null;
    private Class<EntryType> _cls;

    public EntryDetailsLoader(Context context, UUID entryId, Class<EntryType> cls) {
        super(context);

        _entryId = entryId;
        _cls = cls;

        Log.d("[Scriba]", "Initialized EntryDetailsLoader for id=" + _entryId.toString() +
              ", cls=" + _cls);
    }

    @Override
    public void onStartLoading() {
        Log.d("[Scriba]", "EntryDetailsLoader.onStartLoading()");
        forceLoad();
    }

    @Override
    public void onReset() {
        Log.d("[Scriba]", "EntryDetailsLoader.onReset()");
    }

    @Override
    public EntryType loadInBackground() {
        EntryType result = null;

        Log.d("[Scriba]", "EntryDetailsLoader.loadInBackground()");

        ScribaDBManager.useDB(getContext());
        if (_cls == Company.class) {
            result = (EntryType)loadCompany();
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

        ScribaDBManager.releaseDB();
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

    private Company loadCompany() {
        Company comp = ScribaDB.getCompany(_entryId);
        DataDescriptor[] complete_poc_list = comp.poc_list;
        DataDescriptor[] complete_proj_list = comp.proj_list;
        DataDescriptor[] complete_event_list = comp.event_list;
        boolean complete = true;

        if (comp.poc_list[comp.poc_list.length - 1].nextId != DataDescriptor.NONEXT) {
            complete_poc_list = ScribaDB.fetchAll(comp.poc_list);
            complete = false;
        }
        if (comp.proj_list[comp.proj_list.length - 1].nextId != DataDescriptor.NONEXT) {
            complete_proj_list = ScribaDB.fetchAll(comp.proj_list);
            complete = false;
        }
        if (comp.event_list[comp.event_list.length - 1].nextId != DataDescriptor.NONEXT) {
            complete_event_list = ScribaDB.fetchAll(comp.event_list);
            complete = false;
        }

        if (complete) {
            return comp;
        }
        else {
            return new Company(comp.id, comp.name, comp.jur_name, comp.address,
                comp.inn, comp.phonenum, comp.email, complete_poc_list,
                complete_proj_list, complete_event_list);
        }
    }
}
