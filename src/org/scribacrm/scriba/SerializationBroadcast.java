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

import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.util.Log;

// Helper class for communication between Scriba activities and
// SerializationService using Android local broadcast mechanism
public class SerializationBroadcast {

    public static String ACTION_DESERIALIZATION_COMPLETED =
        "Scriba.DESERIALIZATION_COMPLETED";
    public static String ACTION_SERIALIZATION_COMPLETED =
        "Scriba.SERIALIZATION_COMPLETED";

    // register broadcast receiver for deserialization completed event
    public static void registerForDeserialization(Context context,
                                                  BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(ACTION_DESERIALIZATION_COMPLETED);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(receiver, filter);
    }

    // register broadcast receiver for serialization completed event
    public static void registerForSerialization(Context context,
                                                BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(ACTION_SERIALIZATION_COMPLETED);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(receiver, filter);
    }

    // broadcast deserialization completed event
    public static void sendDeserializationBroadcast(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(ACTION_DESERIALIZATION_COMPLETED);
        manager.sendBroadcast(intent);
    }

    // broadcast serialization completed event
    public static void sendSerializationBroadcast(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(ACTION_SERIALIZATION_COMPLETED);
        manager.sendBroadcast(intent);
    }

    // unregister broadcast receiver
    public static void unregisterReceiver(Context context,
                                          BroadcastReceiver receiver) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(receiver);
    }

    // BroadcastReceiver implementation for deserialization event.
    public static class DeserializeReceiver extends BroadcastReceiver {

        private Activity _activity = null;

        public DeserializeReceiver(Activity activity) {
            // activity should take care of being alive and running in
            // foreground when onReceive() executes
            _activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DESERIALIZATION_COMPLETED)) {
                if (_activity != null) {
                    Log.d("[Scriba]", "SerializationBroadcast.DeserializeReceiver" +
                          " recreating activity");
                    _activity.recreate();
                }
            }
        }
    }
}
