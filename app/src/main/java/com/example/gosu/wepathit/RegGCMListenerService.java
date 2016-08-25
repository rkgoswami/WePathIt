package com.example.gosu.wepathit;

/**
 * Created by Rishav on 2/20/2016.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class RegGCMListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    String Ticker;
    Intent intent=null;
    private boolean isInForeground;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */

        //get the value received with the notification
        String RideId = data.getString("RideID");
        String Status = data.getString("RideStatus");
        String DriverName = data.getString("DriverName");
        String DriverMobile = data.getString("DriverMobile");
        String AutoRegNo = data.getString("AutoRegNo");


        if(Status.equals(RideStatus.Ride_Terminated)){
            Ticker="Sorry Ride has been Declined, Kindly Request Again !!!";
            intent=new Intent(getApplicationContext(),MainActivity.class);
        }
        else{
            Ticker = "Ride Confirmed..,Wait for auto to pick you up !!!";
            intent=new Intent(getApplicationContext(),WaitingForRideActivity.class);
        }

        Log.d("Ride detail received ","Ride id in gcm :"+RideId+", Status : "+Status+",DriverName : "+DriverName+",Mobile : "+DriverMobile+",AutoRegNo : "+AutoRegNo);

        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        isInForeground = AppStatus.getBoolean("isInForeground",false);

        Log.d("Foreground"," "+isInForeground);

        if(isInForeground){
            Intent intent = new Intent(this,WaitingForRideActivity.class);
            intent.putExtra("RideID",RideId);
            intent.putExtra("RideStatus",Status);
            intent.putExtra("DriverName",DriverName);
            intent.putExtra("DriverMobile",DriverMobile);
            intent.putExtra("AutoRegNo",AutoRegNo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Log.d("Ride Id","Ride Id in send :"+RideId);
            startActivity(intent);
        }
        else {
            sendNotification(message,RideId,Status,DriverName,DriverMobile,AutoRegNo);
        }
        // [END_EXCLUDE]

    }// [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message,String RideId,String Status,String DriverName,String DriverMobile,String AutoRegNo) {
      //  Intent intent = new Intent(this,WaitingForRideActivity.class);
        intent.putExtra("RideID",RideId);
        Log.d("Ride Id","Ride Id in send :"+RideId);
        intent.putExtra("RideStatus",Status);
        intent.putExtra("DriverName",DriverName);
        intent.putExtra("DriverMobile",DriverMobile);
        intent.putExtra("AutoRegNo",AutoRegNo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("WePathIt message")
                .setSubText(Ticker)
                .setContentText("You have Received a Notification")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(message)
                .setVibrate(new long[]{1000, 1000, 1000})
                .setAutoCancel(true)
                .setTicker(Ticker)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(655 /* ID of notification */, notificationBuilder.build());
    }
}
