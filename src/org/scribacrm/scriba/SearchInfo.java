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

public class SearchInfo {

    public enum SearchType {
        COMPANY_NAME,
        COMPANY_JUR_NAME,
        COMPANY_ADDRESS,
        COMPANY_GENERIC,
        EVENT_COMPANY,
        EVENT_POC,
        EVENT_PROJECT,
        EVENT_GENERIC,
        POC_NAME,
        POC_COMPANY,
        POC_POSITION,
        POC_EMAIL,
        POC_GENERIC,
        PROJECT_COMPANY,
        PROJECT_STATE,
        PROJECT_GENERIC
    }

    private SearchType _searchType;

    // different parameter types for different search types
    private String _stringParam = null;
    private String[] _stringParams = null;
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

    public SearchInfo(SearchType type, String[] params) {
        _searchType = type;
        _stringParams = params;
    }

    public SearchType searchType() { return _searchType; }
    public String stringParam() { return _stringParam; }
    public String[] stringParams() { return _stringParams; }
    public UUID uuidParam() { return _uuidParam; }
    public byte byteParam() { return _byteParam; }
}
