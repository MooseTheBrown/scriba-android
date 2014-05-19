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

// event state mapper maps event state codes defined in libscriba Event class
// to Android resource strings
public class EventStateMapper {
    public static final byte NUM_STATES = 3;

    private Context _context = null;

    public EventStateMapper(Context context) {
        _context = context;
    }

    // get string by event state code
    public String getString(byte state) {
        int resid = -1;
        String ret = null;

        switch (state) {
            case Event.State.SCHEDULED:
                resid = R.string.event_state_scheduled;
                break;
            case Event.State.COMPLETED:
                resid = R.string.event_state_completed;
                break;
            case Event.State.CANCELLED:
                resid = R.string.event_state_cancelled;
                break;
        }

        if (resid != -1) {
            ret = _context.getResources().getString(resid);
        }

        return ret;
    }

    // get event state code by string
    public byte getCode(String stateStr) {
        String cmp = _context.getResources().getString(R.string.event_state_scheduled);
        if (cmp.equals(stateStr)) {
            return Event.State.SCHEDULED;
        }

        cmp = _context.getResources().getString(R.string.event_state_completed);
        if (cmp.equals(stateStr)) {
            return Event.State.COMPLETED;
        }

        cmp = _context.getResources().getString(R.string.event_state_cancelled);
        if (cmp.equals(stateStr)) {
            return Event.State.CANCELLED;
        }

        // nothing found
        return -1;
    }

    // get strings for all possible event states
    public String[] getStrings() {
        String[] ret = new String[NUM_STATES];

        ret[0] = _context.getResources().getString(R.string.event_state_scheduled);
        ret[1] = _context.getResources().getString(R.string.event_state_completed);
        ret[2] = _context.getResources().getString(R.string.event_state_cancelled);

        return ret;
    }
}
