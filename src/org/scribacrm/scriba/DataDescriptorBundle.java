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
import android.os.Bundle;
import java.util.UUID;
import java.util.ArrayList;

/*
 * DataDescriptorBundle supports DataDescriptor array serialization/deserialization
 * to/from Android Bundle. Only UUIDs are serialized, text descriptions are
 * ignored.
 */
public class DataDescriptorBundle {

    private static final String BUNDLE_KEY = "DataDescriptors";
    private static final String NEXTID_KEY = "NextId";

    // serialize array of DataDescriptors to Bundle
    public static Bundle toBundle(DataDescriptor[] array) {
        if (array == null) {
            return null;
        }

        int length = array.length;
        if (array[length - 1].nextId != DataDescriptor.NONEXT) {
            length--;
        }
        String[] UUIDs = new String[length];

        for (int i = 0; i < length; i++) {
            UUIDs[i] = array[i].id.toString();
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray(BUNDLE_KEY, UUIDs);
        if (length < array.length) {
            bundle.putLong(NEXTID_KEY, array[array.length - 1].nextId);
        }

        return bundle;
    }

    // restore array of DataDescriptors from Bundle
    public static DataDescriptor[] fromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        String[] UUIDs = bundle.getStringArray(BUNDLE_KEY);
        DataDescriptor[] array = new DataDescriptor[UUIDs.length];
        ArrayList<DataDescriptor> arrayList = new ArrayList<DataDescriptor>(UUIDs.length);

        for (int i = 0; i < UUIDs.length; i++) {
            DataDescriptor descr = new DataDescriptor(UUID.fromString(UUIDs[i]), null);
            arrayList.add(descr);
        }

        long nextId = bundle.getLong(NEXTID_KEY, DataDescriptor.NONEXT);
        if (nextId != DataDescriptor.NONEXT) {
            DataDescriptor descr = new DataDescriptor(nextId);
            arrayList.add(descr);
        }

        return arrayList.toArray(array);
    }
}
