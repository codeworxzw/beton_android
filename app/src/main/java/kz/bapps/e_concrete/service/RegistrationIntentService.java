package kz.bapps.e_concrete.service;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kz.bapps.e_concrete.EConcrete;
import kz.bapps.e_concrete.JSONParser;
import kz.bapps.e_concrete.R;


public class RegistrationIntentService extends IntentService {

    private static final String LOG_TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private String sessionToken;
    public RegistrationIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences prefs = getSharedPreferences(EConcrete.appName, MODE_PRIVATE);
        sessionToken = prefs.getString("_token","");

        try {
            String authorizedEntity = "1063024416714"; // Project id from Google Developer Console NUMBER:1063024416714
            String scope = "GCM"; // e.g. communicating using GCM, but you can use any
            // URL-safe characters up to a maximum of 1000, or
            // you can also leave it blank.
            String deviceToken = InstanceID.getInstance(this).getToken(authorizedEntity,scope);

//            InstanceID instanceID = InstanceID.getInstance(this);
//            String deviceToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
//                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(LOG_TAG, "GCM Registration Token: " + deviceToken);

            prefs.edit().putString("device_token",deviceToken).commit();

            // Subscribe to topic channels
            subscribeTopics(deviceToken);

            // You should store a boolean that indicates whether the generated deviceToken has been
            // sent to your server. If the boolean is false, send the deviceToken to your server,
            // otherwise your server should have already received the deviceToken.
            prefs.edit()
                    .putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true)
                    .apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            prefs.edit()
                    .putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false)
                    .apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}