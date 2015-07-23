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

import org.scribacrm.libscriba.*;
import android.content.Context;

// CurrencyMapper maps currency codes defined in
// libscriba Project class to Android string resources
public class CurrencyMapper {

    public static final byte NUM_TYPES = 3;

    private Context _context = null;

    public CurrencyMapper(Context context) {
        _context = context;
    }

    // get string representation for given currency code
    public String getString(byte currency) {
        int resid = 0;
        String ret = null;

        switch (currency) {
            case Project.Currency.RUB:
                resid = R.string.currency_rub;
                break;
            case Project.Currency.USD:
                resid = R.string.currency_usd;
                break;
            case Project.Currency.EUR:
                resid = R.string.currency_eur;
                break;
        }

        if (resid != 0) {
            ret = _context.getResources().getString(resid);
        }

        return ret;
    }

    // get currency code for given string representation
    public byte getCode(String str) {

        String cmp = _context.getResources().getString(R.string.currency_rub);
        if (cmp.equals(str)) {
            return Project.Currency.RUB;
        }

        cmp = _context.getResources().getString(R.string.currency_usd);
        if (cmp.equals(str)) {
            return Project.Currency.USD;
        }

        cmp = _context.getResources().getString(R.string.currency_eur);
        if (cmp.equals(str)) {
            return Project.Currency.EUR;
        }

        // nothing found
        return -1;
    }

    // true if str is a string representation of currency code
    public boolean isEqual(byte currency, String str) {
        String cmp = getString(currency);
        if (cmp == null) {
            return false;
        }

        return (cmp.equals(str));
    }

    // get array of strings for all possible currency types
    public String[] getStrings() {
        String[] strs = new String[NUM_TYPES];

        strs[0] = _context.getResources().getString(R.string.currency_rub);
        strs[1] = _context.getResources().getString(R.string.currency_usd);
        strs[2] = _context.getResources().getString(R.string.currency_eur);

        return strs;
    }
}
