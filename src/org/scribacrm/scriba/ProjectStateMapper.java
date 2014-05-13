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

import org.scribacrm.libscriba.*;
import android.content.Context;

// ProjectStateMapper maps project state codes defined in
// libscriba Project class to Android string resources
public class ProjectStateMapper {

    public static final byte NUM_STATES = 8;

    private Context _context = null;

    public ProjectStateMapper(Context context) {
        _context = context;
    }

    // get string representation for given state code
    public String getString(byte state) {
        int resid = 0;
        String ret = null;

        switch (state) {
            case Project.State.INITIAL:
                resid = R.string.proj_state_initial;
                break;
            case Project.State.CLIENT_INFORMED:
                resid = R.string.proj_state_client_informed;
                break;
            case Project.State.CLIENT_RESPONSE:
                resid = R.string.proj_state_client_response;
                break;
            case Project.State.OFFER:
                resid = R.string.proj_state_offer;
                break;
            case Project.State.REJECTED:
                resid = R.string.proj_state_rejected;
                break;
            case Project.State.CONTRACT_SIGNED:
                resid = R.string.proj_state_contract_signed;
                break;
            case Project.State.EXECUTION:
                resid = R.string.proj_state_execution;
                break;
            case Project.State.PAYMENT:
                resid = R.string.proj_state_payment;
                break;
        }

        if (resid != 0) {
            ret = _context.getResources().getString(resid);
        }

        return ret;
    }

    // get state code for given string representation
    public byte getCode(String str) {

        String cmp = _context.getResources().getString(R.string.proj_state_initial);
        if (cmp.equals(str)) {
            return Project.State.INITIAL;
        }

        cmp = _context.getResources().getString(R.string.proj_state_client_informed);
        if (cmp.equals(str)) {
            return Project.State.CLIENT_INFORMED;
        }

        cmp = _context.getResources().getString(R.string.proj_state_client_response);
        if (cmp.equals(str)) {
            return Project.State.CLIENT_RESPONSE;
        }

        cmp = _context.getResources().getString(R.string.proj_state_offer);
        if (cmp.equals(str)) {
            return Project.State.OFFER;
        }

        cmp = _context.getResources().getString(R.string.proj_state_rejected);
        if (cmp.equals(str)) {
            return Project.State.REJECTED;
        }

        cmp = _context.getResources().getString(R.string.proj_state_contract_signed);
        if (cmp.equals(str)) {
            return Project.State.CONTRACT_SIGNED;
        }

        cmp = _context.getResources().getString(R.string.proj_state_execution);
        if (cmp.equals(str)) {
            return Project.State.EXECUTION;
        }

        cmp = _context.getResources().getString(R.string.proj_state_payment);
        if (cmp.equals(str)) {
            return Project.State.PAYMENT;
        }

        // nothing found
        return -1;
    }

    // true if str is a string representation of state
    public boolean isEqual(byte state, String str) {
        String cmp = getString(state);
        if (cmp == null) {
            return false;
        }

        return (cmp.equals(str));
    }

    // get array of strings for all possible state codes
    public String[] getStrings() {
        String[] strs = new String[NUM_STATES];

        strs[0] = _context.getResources().getString(R.string.proj_state_initial);
        strs[1] = _context.getResources().getString(R.string.proj_state_client_informed);
        strs[2] = _context.getResources().getString(R.string.proj_state_client_response);
        strs[3] = _context.getResources().getString(R.string.proj_state_offer);
        strs[4] = _context.getResources().getString(R.string.proj_state_rejected);
        strs[5] = _context.getResources().getString(R.string.proj_state_contract_signed);
        strs[6] = _context.getResources().getString(R.string.proj_state_execution);
        strs[7] = _context.getResources().getString(R.string.proj_state_payment);

        return strs;
    }
}
