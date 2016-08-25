package com.example.gosu.wepathit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Button rideRequest;
    String responseStr;
    String postReceiverUrl="http://www.wepathit.com/API/V1/Ride/Put.php";
    String userDestination,userDestLat,userDestLong;
    String userSource,userSrcLat,userSrcLong;
    TextView tvSource,tvDestination;
    String YourSrc="Your Location";
    private View myDetailLayout,myPostDetailLayout;
    TextView tvWaitMessage;
    //shared preference
    private boolean isInForeground;

    String RegId_File="Reg_Id_File";
    LocationManager locationManager;
    Location location;
    public double myLatitude,myLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting the application Status as true
        isInForeground=true;
        locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //get the current location
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        Log.d("Location","lat : "+myLatitude+"long :"+myLongitude);

        //get the value passed to the activity in intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            userDestination = extras.getString("DestinationName");
            userDestLat = extras.getString("userDestLat");
            userDestLong = extras.getString("userDestLong");
        }

        //set the detail on layout screen
        tvSource=(TextView)findViewById(R.id.tvSource);
        tvDestination=(TextView)findViewById(R.id.tvDestination);

        tvSource.setText("Source :  "+YourSrc);
        tvDestination.setText("Destination :  "+userDestination);

        myDetailLayout = findViewById(R.id.LayoutDetail);
        myPostDetailLayout = findViewById(R.id.LayoutPostDetail);
        tvWaitMessage=(TextView)findViewById(R.id.waitingMessage);
        rideRequest = (Button)findViewById(R.id.ride_button);
        rideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postLocationData();
                myDetailLayout.setVisibility(View.GONE);
                myPostDetailLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //set title
        alertDialogBuilder.setTitle("Alert!!");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher);

        //set the dialog message
        alertDialogBuilder
                .setMessage("Do you want to exit the app?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        //Intent intent = new Intent(getApplicationContext(),PlacePickerActivity.class);
                        //startActivity(intent);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;
        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        AppStatus.edit().putBoolean("isInForeground",isInForeground).apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        AppStatus.edit().putBoolean("isInForeground",isInForeground).apply();
    }
    @Override
    protected void onStop() {
        super.onStop();
        isInForeground = false;
        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        AppStatus.edit().putBoolean("isInForeground",isInForeground).apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isInForeground = true;
        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        AppStatus.edit().putBoolean("isInForeground",isInForeground).apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isInForeground = true;
        SharedPreferences AppStatus = getSharedPreferences("AppStatus",0);
        AppStatus.edit().putBoolean("isInForeground",isInForeground).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void postLocationData()
    {
        class PostLocationDataOnServer extends AsyncTask<Void,Void,String>
        {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    //send data to server to authenticate
                    HttpClient httpClient = new DefaultHttpClient();
                    //post header
                    HttpPost httpPost = new HttpPost(postReceiverUrl);

                    //add data

                    List<NameValuePair>nameValuePairs =new ArrayList<>(2);

                    SharedPreferences RegIdExtract = getSharedPreferences(RegId_File,0);
                    String Id = RegIdExtract.getString("RegId", null);
                    Log.d("RegId in MainActivity ","RegId :"+Id);

                    nameValuePairs.add(new BasicNameValuePair("ID", Id));
                    nameValuePairs.add(new BasicNameValuePair("pickupLat",""+myLatitude));
                    nameValuePairs.add(new BasicNameValuePair("pickupLong",""+myLongitude));
                    nameValuePairs.add(new BasicNameValuePair("destLat",userDestLat));
                    nameValuePairs.add(new BasicNameValuePair("destLong",userDestLong));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    //execute the post request
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity resEntity = httpResponse.getEntity();

                    responseStr = EntityUtils.toString(resEntity).trim();
                    /*if(resEntity != null){
                        responseStr = EntityUtils.toString(resEntity).trim();
                        Log.d("Response","Response : "+responseStr);
                    }*/


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return responseStr;
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), responseStr, Toast.LENGTH_LONG).show();
                Log.d("output", "Response : " + responseStr);
                if(responseStr.contains("True")) {
                    Log.d("output","Response for ride request inside the if :"+responseStr);
                   // Intent i = new Intent(getApplicationContext(), WaitingForRideActivity.class);
                    //startActivity(i);
                    //finish();
                    tvWaitMessage.setText(R.string.waitingMessage);

                }
                else {
                    Log.d("output", "Response for ride request outside the if : " + responseStr);
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                   // alert.showAlertDialog(MainActivity.this, "Login failed..", "Please enter username and emailId", false);
                }

            }
        }

        new PostLocationDataOnServer().execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        userSrcLong=""+location.getLongitude();
        userSrcLat=""+location.getLatitude();
        Geocoder gcd =new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address>addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(addresses.size()>0){
                userSource=addresses.get(0).getLocality();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(),"Lat: "+userSrcLat+" Long: "+userSrcLong+" City: "+userSource,Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(this);
        locationManager=null;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
