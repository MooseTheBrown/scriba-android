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

import java.util.UUID;
import android.os.Bundle;

public class SearchInfo {

    public enum SearchType {
        COMPANY_NAME(0),
        COMPANY_JUR_NAME(1),
        COMPANY_ADDRESS(2),
        EVENT_DESCR(3),
        EVENT_COMPANY(4),
        EVENT_POC(5),
        EVENT_PROJECT(6),
        POC_NAME(7),
        POC_COMPANY(8),
        POC_POSITION(9),
        POC_EMAIL(10),
        PROJECT_TITLE(11),
        PROJECT_COMPANY(12),
        PROJECT_STATE(13);

        private final int _id;

        SearchType(int id) { _id = id; }

        public int getId() { return _id; }
    }

    private static final String SEARCH_TYPE_KEY = "SearchType";
    private static final String STRING_PARAM_KEY = "StringParam";
    private static final String UUID_PARAM_KEY = "UUIDParam";
    private static final String BYTE_PARAM_KEY = "ByteParam";

    private SearchType _searchType;

    // different parameter types for different search types
    private String _stringParam = null;
    private UUID _uuidParam = null;
    private byte _byteParam = 0;

    public SearchInfo(SearchType type, String param) {
        _searchType = type;
        _stringParam = param;
    }

    public SearchInfo(SearchType type, UUID param) {
        _searchType = type;
        _uuidParam = param;
    }

    public SearchInfo(SearchType type, byte param) {
        _searchType = type;
        _byteParam = param;
    }

    public void setString(String param) { _stringParam = param; }
    public void setUUID(UUID param) { _uuidParam = param; }
    public void setByte(byte param) { _byteParam = param; }

    public SearchType searchType() { return _searchType; }
    public String stringParam() { return _stringParam; }
    public UUID uuidParam() { return _uuidParam; }
    public byte byteParam() { return _byteParam; }

    // save SearchInfo data in Bundle
    public void toBundle(Bundle bundle) {
        bundle.putInt(SEARCH_TYPE_KEY, _searchType.getId());
        bundle.putString(STRING_PARAM_KEY, _stringParam);
        bundle.putString(UUID_PARAM_KEY, _uuidParam.toString());
        bundle.putByte(BYTE_PARAM_KEY, _byteParam);
    }

    // restore SearchInfo data from Bundle
    public static SearchInfo fromBundle(Bundle bundle) {
        int searchTypeId = bundle.getInt(SEARCH_TYPE_KEY, SearchType.COMPANY_NAME.getId());
        SearchType searchType = SearchType.COMPANY_NAME;
        for (SearchType t : SearchType.values()) {
            if (t.getId() == searchTypeId) {
                searchType = t;
                break;
            }
        }

        String stringParam = bundle.getString(STRING_PARAM_KEY, null);
        if (stringParam != null) {
            return new SearchInfo(searchType, stringParam);
        }

        String uuidString = bundle.getString(UUID_PARAM_KEY, null);
        if (uuidString != null) {
            return new SearchInfo(searchType, UUID.fromString(uuidString));
        }

        byte byteParam = bundle.getByte(BYTE_PARAM_KEY);
        return new SearchInfo(searchType, byteParam);
    }
}
