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

import org.scribacrm.libscriba.ScribaDB;
import android.util.Log;
import android.content.Context;

/* Before calling any method from ScribaDB every component should first call
 * ScribaDBManager.useDB() to make sure that the database is initialized and ready.
 * Once database is no longer needed, component may call releaseDB() to close
 * the database if it's not used by anyone else. */
public class ScribaDBManager {

    private static final String DB_FILE_NAME = "scriba.db";

    private static int users = 0;

    public static synchronized void useDB(Context context) {
        Log.d("[Scriba]", "useDB(), users = " + users);
        if (users == 0) {
            ScribaDB.DBDescr descr = new ScribaDB.DBDescr();
            descr.name = "scriba_sqlite";
            descr.type = ScribaDB.DBType.BUILTIN;

            ScribaDB.DBParam[] params = new ScribaDB.DBParam[1];
            params[0] = new ScribaDB.DBParam();
            params[0].key = "db_loc";
            String db_location = context.getFilesDir().getAbsolutePath() + "/" + DB_FILE_NAME;
            params[0].value = db_location;
            Log.d("[Scriba]", "Trying to init scriba DB using db file " + db_location);

            int ret = ScribaDB.init(descr, params);
            Log.d("[Scriba]", "ScribaDB.init() returned " + ret);
        }
        users++;
    }

    public static synchronized void useDBNosync(Context context) {
        if (users == 0) {
            ScribaDB.DBDescr descr = new ScribaDB.DBDescr();
            descr.name = "scriba_sqlite";
            descr.type = ScribaDB.DBType.BUILTIN;

            ScribaDB.DBParam[] params = new ScribaDB.DBParam[2];
            params[0] = new ScribaDB.DBParam();
            params[0].key = "db_loc";
            String db_location = context.getFilesDir().getAbsolutePath() + "/" + DB_FILE_NAME;
            params[0].value = db_location;
            params[1] = new ScribaDB.DBParam();
            params[1].key = "db_sync";
            params[1].value = "off";

            Log.d("[Scriba]", "Trying to init scriba DB using db file " + db_location);
            int ret = ScribaDB.init(descr, params);
            Log.d("[Scriba]", "ScribaDB.init() returned " + ret);
        }
        users++;
    }

    public static synchronized void releaseDB() {
        Log.d("[Scriba]", "releaseDB(), users = " + users);
        if (users > 0) {
            users--;
            if (users == 0) {
                Log.d("[Scriba]", "Calling ScribaDB.cleanup()");
                ScribaDB.cleanup();
            }
        }
    }
}
