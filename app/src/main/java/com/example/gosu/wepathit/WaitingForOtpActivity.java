package com.example.gosu.wepathit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

public class WaitingForOtpActivity extends AppCompatActivity {

    String postReceivedOTP="http://www.wepathit.com/API/V1/Utility/CustomerOTPVerify.php";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    EditText etMobile,etOtp;
    Button btnSubmit;
    String phone;
    int OTPcount=0;

    //shared preference
    String RegId_File="Reg_Id_File";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_otp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            phone= intent.getExtras().getString("phone");
        }

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent i = new Intent(this, RegistrationIntentService.class);
            i.putExtra("mobile",phone);
            startService(i);
        }
        //instantiate the variables

        etMobile=(EditText)findViewById(R.id.etMobile);
        etOtp=(EditText)findViewById(R.id.etOtp);
        btnSubmit=(Button)findViewById(R.id.btnSubmit);

        etMobile.setText(phone);
        //post data to server for storing in database of new
        // postUserData();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //post otp to server for verification
                String otpReceived= etOtp.getText().toString();
                OTPcount++;
                String otpCount=Integer.toString(OTPcount);
                postOtp(otpReceived, phone,otpCount);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void postOtp(String tempOtp,String tempMobile,String tempOtpCount){
        final String otp = tempOtp;
        final String mobile=tempMobile;
        final String otpCount=tempOtpCount;
        //final Integer otpInt=1234;
        class postOtpOnServer extends AsyncTask<List<NameValuePair>,Void,String> {
            String responseForOtp=null;
            @Override
            protected String doInBackground(List<NameValuePair>... params) {

                try{
                    //setUp connection to server
                    HttpClient httpClient = new DefaultHttpClient();

                    //setUp post header
                    HttpPost httpPost = new HttpPost(postReceivedOTP);

                    //create name value pair in List
                    List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                    nameValuePairs.add(new BasicNameValuePair("mobile",mobile));
                    nameValuePairs.add(new BasicNameValuePair("OTP",otp));
                    nameValuePairs.add(new BasicNameValuePair("OTPCount",otpCount));

                    //setUp Entity
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    //execute the post request
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();

                    responseForOtp = EntityUtils.toString(httpEntity);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("Otp received", "Otp Response in post data : " + responseForOtp);
                return responseForOtp;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d("OtpRegistration ", "Otp Response : " + responseForOtp);
                //Toast.makeText(getApplicationContext(), responseForOtp, Toast.LENGTH_LONG).show();
                if(responseForOtp.contains("Success")){
                    Log.d("Otp", "Otp verification  : " + responseForOtp);
                    Toast.makeText(getApplicationContext(), responseForOtp, Toast.LENGTH_LONG).show();
                    Intent intent =new Intent(getApplicationContext(),LoginActivity.class);

                    //Splitting the response
                    String[] pieces = responseForOtp.split(":");
                    Log.d("Reg Id", "pieces :"+pieces[0]+","+pieces[1]);
                    SharedPreferences RegIdShared = getSharedPreferences(RegId_File, 0);
                    RegIdShared.edit().putString("RegId",pieces[1]).apply();

                    startActivity(intent);
                    finish();
                }
                else if(responseForOtp.equals("Failed")){
                    Toast.makeText(getApplicationContext(),responseForOtp, Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),responseForOtp, Toast.LENGTH_LONG).show();
                    Intent intent =new Intent(getApplicationContext(),RegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
        new postOtpOnServer().execute();
    }

}
