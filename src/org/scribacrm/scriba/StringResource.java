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

import android.content.Context;

public final class StringResource {

    private final int _resid;
    private final Context _context;

    StringResource(Context context, int resid) {
        _context = context;
        _resid = resid;
    }

    public int resid() { return _resid; }

    @Override
    public String toString() {
        if (_context != null) {
            return _context.getResources().getString(_resid);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StringResource)) {
            return false;
        }

        StringResource res = (StringResource)o;
        if (res.resid() == _resid) {
            return true;
        }

        return false;
    }
}
