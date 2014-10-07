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
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

public class SerializationService extends IntentService {

    /*
     * SerializationService API.
     *
     * The service handles 2 types of requests: serialize data and
     * restore data.
     *
     * For serialize request it expects four arrays of UUIDs: companies, events,
     * people and projects. Each array should be packed to a Bundle created using
     * DataDescriptorBundle class. All four Bundles should be packed into another
     * Bundle, which should be put into Intent passed to the service.
     *
     * Deserialize request should also have Bundle packed into Intent. This Bundle
     * should contain a single value - merge strategy type (integer).
     *
     * Both requests should contain String values specifying file to store (in case
     * of serialize request) or retrieve binary data from (in case of deserialize
     * request).
     */

    // Intent keys
    public static final String REQUEST_TYPE_KEY = "rq_type";
    public static final String REQUEST_DATA_KEY = "rq_data";
    public static final String REQUEST_FILE_KEY = "rq_file";

    // Intent constants
    public static final byte REQUEST_TYPE_SERIALIZE = 0;
    public static final byte REQUEST_TYPE_DESERIALIZE = 1;

    // Request data Bundle keys
    public static final String REQUEST_DATA_COMPANIES = "rq_companies";
    public static final String REQUEST_DATA_EVENTS = "rq_events";
    public static final String REQUEST_DATA_PEOPLE = "rq_people";
    public static final String REQUEST_DATA_PROJECTS = "rq_projects";
    public static final String REQUEST_DATA_MERGESTRAT = "rq_mergestrat";

    // notification ID
    private static final int NOTIFICATION_ID = 0;

    // create valid serialize request for this service
    public static Intent serializeRequest(Context context,
                                          DataDescriptor[] companies,
                                          DataDescriptor[] events,
                                          DataDescriptor[] people,
                                          DataDescriptor[] projects,
                                          String filename) {
        if (context == null) {
            return null;
        }

        Intent intent = new Intent(context, SerializationService.class);

        intent.putExtra(REQUEST_TYPE_KEY, REQUEST_TYPE_SERIALIZE);

        Bundle company_bundle = DataDescriptorBundle.toBundle(companies);
        Bundle event_bundle = DataDescriptorBundle.toBundle(events);
        Bundle poc_bundle = DataDescriptorBundle.toBundle(people);
        Bundle project_bundle = DataDescriptorBundle.toBundle(projects);

        Bundle rqData = new Bundle();
        rqData.putBundle(REQUEST_DATA_COMPANIES, company_bundle);
        rqData.putBundle(REQUEST_DATA_EVENTS, event_bundle);
        rqData.putBundle(REQUEST_DATA_PEOPLE, poc_bundle);
        rqData.putBundle(REQUEST_DATA_PROJECTS, project_bundle);

        intent.putExtra(REQUEST_DATA_KEY, rqData);

        intent.putExtra(REQUEST_FILE_KEY, filename);

        return intent;
    }

    // create valid deserialize request for this service
    public static Intent deserializeRequest(Context context,
                                            byte mergeStrat,
                                            String filename) {
        if (context == null) {
            return null;
        }

        Intent intent = new Intent(context, SerializationService.class);

        intent.putExtra(REQUEST_TYPE_KEY, REQUEST_TYPE_DESERIALIZE);

        Bundle rqData = new Bundle();
        rqData.putByte(REQUEST_DATA_MERGESTRAT, mergeStrat);

        intent.putExtra(REQUEST_DATA_KEY, rqData);

        intent.putExtra(REQUEST_FILE_KEY, filename);

        return intent;
    }

    public SerializationService() {
        super("SerializationThread");
    }

    @Override
    public void onCreate() {
        ScribaDBManager.useDB(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        ScribaDBManager.releaseDB();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get request type, request data and file from the intent
        byte rqType = intent.getByteExtra(REQUEST_TYPE_KEY, REQUEST_TYPE_SERIALIZE);
        Bundle rqData = intent.getBundleExtra(REQUEST_DATA_KEY);
        String filename = intent.getStringExtra(REQUEST_FILE_KEY);

        if ((rqData == null) || (filename == null) ||
            (rqType < REQUEST_TYPE_SERIALIZE) || (rqType > REQUEST_TYPE_DESERIALIZE)) {
            // invalid request
            Log.e("[Scriba]", "SerializationService received invalid serialize request");
            return;
        }

        // act according to request type
        if (rqType == REQUEST_TYPE_SERIALIZE) {
            serialize(rqData, filename);
        }
        else {
            deserialize(rqData, filename);
        }
    }

    private void serialize(Bundle rqData, String filename) {
        Bundle company_bundle = rqData.getBundle(REQUEST_DATA_COMPANIES);
        Bundle event_bundle = rqData.getBundle(REQUEST_DATA_EVENTS);
        Bundle poc_bundle = rqData.getBundle(REQUEST_DATA_PEOPLE);
        Bundle project_bundle = rqData.getBundle(REQUEST_DATA_PROJECTS);

        DataDescriptor[] companies = DataDescriptorBundle.fromBundle(company_bundle);
        DataDescriptor[] events = DataDescriptorBundle.fromBundle(event_bundle);
        DataDescriptor[] people = DataDescriptorBundle.fromBundle(poc_bundle);
        DataDescriptor[] projects = DataDescriptorBundle.fromBundle(project_bundle);

        byte[] data = ScribaDB.serialize(companies, events, people, projects);

        File file = new File(filename);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data, 0, data.length);
            stream.close();
            notifyCompleted(R.string.notify_serialize_ok);
        }
        catch (IOException e) {
            Log.d("[Scriba]", "SerializationService caught IOException: " + e.getMessage());
            notifyCompleted(R.string.notify_serialize_failed);
        }
    }

    private void deserialize(Bundle rqData, String filename) {
        byte mergeStrat = rqData.getByte(REQUEST_DATA_MERGESTRAT,
                                         ScribaDB.MergeStrategy.LOCAL_OVERRIDE);

        if ((mergeStrat < ScribaDB.MergeStrategy.LOCAL_OVERRIDE) ||
            (mergeStrat > ScribaDB.MergeStrategy.MANUAL)) {
            // invalid request
            Log.e("[Scriba]", "SerializationService received invalid deserialize request");
            return;
        }

        File file = new File(filename);
        long filesize = file.length();
        byte[] data = new byte[(int)filesize];
        try {
            FileInputStream stream = new FileInputStream(file);
            stream.read(data, 0, (int)filesize);
            stream.close();
            ScribaDB.deserialize(data, mergeStrat);
            notifyCompleted(R.string.notify_deserialize_ok);
            // notify activities that are interested that data has been
            // imported
            SerializationBroadcast.sendDeserializationBroadcast(this);
        }
        catch (IOException e) {
            Log.d("[Scriba]", "SerializationService caught IOException: " + e.getMessage());
            notifyCompleted(R.string.notify_deserialize_failed);
        }
    }

    // show notification indicating completed data sync (either successful or failed)
    private void notifyCompleted(int msgId) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.notify_file_sync_title));
        builder.setContentText(getString(msgId));
        builder.setSmallIcon(R.drawable.scriba_icon);
        Intent intent = new Intent(this, EntryListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                                          0,
                                          intent,
                                          PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        Notification notification = builder.build();

        NotificationManager nm = (NotificationManager)
                                 getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
    }
}
