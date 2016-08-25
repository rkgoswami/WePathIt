package com.example.gosu.wepathit;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rishav on 2/20/2016.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private static final String SenderId ="975296929083";
    private static final String API_KEY ="AIzaSyDyBvb6gOEqyrT5uzB_FI4Z8Q8eVVluAPs";


    String postReceiverUrl="http://www.wepathit.com/API/V1/Utility/CustomerGCMUpdate.php";


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String phone=null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            phone= intent.getExtras().getString("mobile");
            Log.d("mobile", "" + phone);
        }

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
            String token = instanceID.getToken(SenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE,null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token,phone);

            // Subscribe to topic channels
            // subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token,String phone) {
        // Add custom implementation, as needed.
        Log.d("Token Received", " " + token);

        postGcmData(token,phone);
    }


    public void postGcmData(String tempToken,String tempPhone){
        final String token=tempToken;
        final String phone=tempPhone;
        class postDataOnServer extends AsyncTask<List<NameValuePair>,Void,String> {
            String responseStr=null;
            @Override
            protected String doInBackground(List<NameValuePair>... params) {

                try{
                    //setUp connection to server
                    HttpClient httpClient = new DefaultHttpClient();

                    //setUp post header
                    HttpPost httpPost = new HttpPost(postReceiverUrl);

                    //create name value pair in List
                    List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                    nameValuePairs.add(new BasicNameValuePair("mobile",phone));
                    nameValuePairs.add(new BasicNameValuePair("GCMID",token));


                    //setUp Entity
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    //execute the post request
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();

                    responseStr = EntityUtils.toString(httpEntity);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return responseStr;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.d("Registration GCM", "Response : " + responseStr);
                Toast.makeText(getApplicationContext(), responseStr, Toast.LENGTH_LONG).show();

            }
        }
        new postDataOnServer().execute();
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
            //  pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
