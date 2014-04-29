package org.scribacrm.scriba;

import org.scribacrm.libscriba.ScribaDB;
import android.util.Log;
import android.content.Context;

/* Before calling any method from ScribaDB every component should first call
 * ScribaDBManager.useDB() to make sure that the database is initialized and ready.
 * Once database is no longer needed, component may call releaseDB() to close
 * the database if it's not used by anyone else.
 * Note that this class is not thread-safe, its methods have to be called on
 * UI thread only. */
public class ScribaDBManager {

    private static final String DB_FILE_NAME = "scriba.db";

    private static int users = 0;

    public static void useDB(Context context) {
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

    public static void releaseDB() {
        if (users > 0) {
            users--;
        }
        if (users == 0) {
            Log.d("[Scriba]", "Calling ScribaDB.cleanup()");
            ScribaDB.cleanup();
        }
    }
}
