package com.example.gosu.wepathit;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class WaitingForRideActivity extends AppCompatActivity {


    Button btnConfirm,btnCancel;
    String RideId,MobileNo;//="8861856892";
    String DriverName,AutoRegNo;// = "Shyam";
    String postReceiverUrl="http://www.wepathit.com/API/V1/Ride/CustomerResponse.php";
    TextView tvDriverName,tvDriverMobile,tvAutoNo;
    View PreConfirm,PostConfirm;
   // ImageButton btnCallDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_ride);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get the value passed to the activity in intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //read the bundle data
            RideId= intent.getExtras().getString("RideID");
            DriverName= intent.getExtras().getString("DriverName");
            MobileNo= intent.getExtras().getString("DriverMobile");
            AutoRegNo= intent.getExtras().getString("AutoRegNo");
        }

        //detail need to displayed
        //tvRideID=(TextView)findViewById(R.id.tvRideId);
        tvDriverName=(TextView)findViewById(R.id.tvDriver);
        tvDriverMobile=(TextView)findViewById(R.id.tvDriverMob);
        tvAutoNo = (TextView)findViewById(R.id.tvAutoRegNo);

        //set detail on the layout screen
        //tvRideID.setText(RideId);
        tvDriverName.setText(DriverName);
        tvDriverMobile.setText(MobileNo);
        tvAutoNo.setText(AutoRegNo);

        final String telMobile = "tel:"+MobileNo;
        //call button facility to the customer
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(telMobile));
                startActivity(callIntent);
            }
        });



        //Button to Confirm the ride
        btnConfirm=(Button)findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postConfirmation(RideStatus.Ride_Confirmed);
            }
        });

        //Button to Cancel the ride
        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postConfirmation(RideStatus.Ride_Cancelled);
            }
        });

        //Layout view before and after the confirmation
        PreConfirm = findViewById(R.id.LayoutPreConfirm);
        PostConfirm = findViewById(R.id.LayoutPostConfirm);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //set title
        alertDialogBuilder.setTitle("Alert!!");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher);

        //set the dialog message
        alertDialogBuilder
                .setMessage("Want to cancel the ride")
                .setCancelable(false)
                .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        Intent intent = new Intent(getApplicationContext(),PlacePickerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        //call alert box
        //finish();
       // alertBox();
    }

    void alertBox(){
        //alert dialog

    }

    public void postConfirmation(final String Status){
        class postConfirmationOfRide extends AsyncTask<Void,Void,String> {
            String responseStr=null;
            @Override
            protected String doInBackground(Void... params) {

                try{
                    //setUp connection to server
                    HttpClient httpClient = new DefaultHttpClient();

                    //setUp post header
                    HttpPost httpPost = new HttpPost(postReceiverUrl);

                    //create name value pair in List
                    Log.d("Ride ID","Id received in Waiting For ride: "+RideId);
                    List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                    nameValuePairs.add(new BasicNameValuePair("RideID",RideId));
                    nameValuePairs.add(new BasicNameValuePair("RideStatus",Status));


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
                if(responseStr.contains("TRUE")){
                    Log.d("Ride Request ", "Response in Ride Wait: " + responseStr);
                    Toast.makeText(getApplicationContext(), responseStr, Toast.LENGTH_LONG).show();
                    //btnCancel.setVisibility(View.GONE);
                    //btnConfirm.setText(R.string.action_start_ride);
                    PreConfirm.setVisibility(View.GONE);
                    PostConfirm.setVisibility(View.VISIBLE);

                }else{
                    Log.d("Ride Request", "Response : " + responseStr);
                    Toast.makeText(getApplicationContext(), responseStr, Toast.LENGTH_LONG).show();
                }
            }
        }
        new postConfirmationOfRide().execute();
    }

}
