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

import android.net.Uri;
import java.util.UUID;
import java.lang.UnsupportedOperationException;
import android.util.Log;

// EntryURI class handles URI representation of entry type and id
public class EntryURI {

    private EntryType _type;
    private UUID _id;

    public static final String SCHEME = "scriba";
    public static final String COMPANY_PATH = "company";
    public static final String EVENT_PATH = "event";
    public static final String POC_PATH = "poc";
    public static final String PROJECT_PATH = "project";
    public static final String ID_KEY = "id";

    public EntryURI(Uri uri) {
        _type = null;
        _id = null;

        try {
            if (uri.getScheme().equals(SCHEME)) {
                String path = uri.getPath();
                if (path.equals(COMPANY_PATH)) {
                    _type = EntryType.COMPANY;
                }
                else if (path.equals(EVENT_PATH)) {
                    _type = EntryType.EVENT;
                }
                else if (path.equals(POC_PATH)) {
                    _type = EntryType.POC;
                }
                else if (path.equals(PROJECT_PATH)) {
                    _type = EntryType.PROJECT;
                }
                else {
                    Log.e("[Scriba]", "EntryURI(uri): invalid path " + path);
                }

                String idStr = uri.getQueryParameter(ID_KEY);
                if (idStr != null) {
                    _id = UUID.fromString(idStr);
                }
                else {
                    Log.e("[Scriba]", "EntryURI(uri): id parameter not found");
                }
            }
            else {
                Log.e("[Scriba]", "EntryURI(uri): unsupported scheme " + uri.getScheme());
            }
        }
        catch (UnsupportedOperationException e) {
            Log.e("[Scriba]", "EntryURI failed to parse URI: " + e.getMessage());
        }
    }

    public EntryURI(EntryType type, UUID id) {
        _type = type;
        _id = id;
    }

    public Uri getUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        switch (_type) {
            case COMPANY:
                builder.appendPath(COMPANY_PATH);
                break;
            case EVENT:
                builder.appendPath(EVENT_PATH);
                break;
            case POC:
                builder.appendPath(POC_PATH);
                break;
            case PROJECT:
                builder.appendPath(PROJECT_PATH);
                break;
        }
        builder.appendQueryParameter(ID_KEY, _id.toString());

        try {
            return builder.build();
        }
        catch (UnsupportedOperationException e) {
            Log.e("[Scriba]", "EntryURI failed to build URI: " + e.getMessage());
            return null;
        }
    }

    public EntryType getType() {
        return _type;
    }

    public UUID getId() {
        return _id;
    }
}
