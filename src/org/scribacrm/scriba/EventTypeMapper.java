package org.scribacrm.scriba;

import org.scribacrm.libscriba.*;
import android.content.Context;

// event type mapper maps event codes defined in libscriba Event class
// to Android resource strings
public class EventTypeMapper {

    public static final byte NUM_TYPES = 3;

    private Context _context = null;

    public EventTypeMapper(Context context) {
        _context = context;
    }

    // get string by type code
    public String getString(byte type) {
        int resid = -1;
        String ret = null;

        switch (type) {
            case Event.Type.MEETING:
                resid = R.string.event_type_meeting;
                break;
            case Event.Type.CALL:
                resid = R.string.event_type_call;
                break;
            case Event.Type.TASK:
                resid = R.string.event_type_task;
                break;
        }

        if (resid != -1) {
            ret = _context.getResources().getString(resid);
        }

        return ret;
    }

    // get type code by string
    public byte getCode(String typeStr) {
        String cmp = _context.getResources().getString(R.string.event_type_meeting);
        if (cmp.equals(typeStr)) {
            return Event.Type.MEETING;
        }

        cmp = _context.getResources().getString(R.string.event_type_call);
        if (cmp.equals(typeStr)) {
            return Event.Type.CALL;
        }

        cmp = _context.getResources().getString(R.string.event_type_task);
        if (cmp.equals(typeStr)) {
            return Event.Type.TASK;
        }

        // nothing found
        return -1;
    }

    // get strings for all possible types
    public String[] getStrings() {
        String[] ret = new String[NUM_TYPES];

        ret[0] = _context.getResources().getString(R.string.event_type_meeting);
        ret[1] = _context.getResources().getString(R.string.event_type_call);
        ret[2] = _context.getResources().getString(R.string.event_type_task);

        return ret;
    }
}
