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

    // serialize array of DataDescriptors to Bundle
    public static Bundle toBundle(DataDescriptor[] array) {
        if (array == null) {
            return null;
        }

        String[] UUIDs = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            UUIDs[i] = array[i].id.toString();
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray(BUNDLE_KEY, UUIDs);

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

        return arrayList.toArray(array);
    }
}
