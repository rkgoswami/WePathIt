package com.example.gosu.wepathit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

public class RegistrationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    String postReceiverUrl="http://www.wepathit.com/API/V1/CustomerLogin/Signup.php";
    // UI references.
    private AutoCompleteTextView mNameView,mEmailView,mPasswordView,mConfirmPasswordView;
    private EditText mMobileView;
    private View mProgressView;
    private View mRegisterFormView;
    String responseStr;

    private UserRegistrationTask mAuthTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the registration form.
        mNameView = (AutoCompleteTextView) findViewById(R.id.RegName);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.RegEmail);
        mPasswordView = (AutoCompleteTextView) findViewById(R.id.RegPassword);
        mConfirmPasswordView = (AutoCompleteTextView) findViewById(R.id.RegConfirmPassword);
        mMobileView = (EditText) findViewById(R.id.RegMobile);

        //Register Button
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();

            }
        });

        mRegisterFormView = findViewById(R.id.Registration_form);
        mProgressView = findViewById(R.id.registration_progress);

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);
        mMobileView.setError(null);

        // Store values at the time of the login attempt.
        String name = mEmailView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirm_password = mConfirmPasswordView.getText().toString();
        String mobile = mMobileView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a password matches with confirmation, if the user entered the Confirm password.
        if (!TextUtils.isEmpty(confirm_password) && !isConfirmPasswordValid(password,confirm_password)) {
            mConfirmPasswordView.setError(getString(R.string.error_mismatch_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a empty name field.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        // Check for a valid mobile field.
        if (TextUtils.isEmpty(mobile)) {
            mMobileView.setError(getString(R.string.error_field_required));
            focusView = mMobileView;
            cancel = true;
        } else if (!isMobileNumberValid(mobile)) {
            mMobileView.setError(getString(R.string.error_invalid_mobile_no));
            focusView = mMobileView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegistrationTask(name,email,password,mobile);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {

        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {

        return password.length() > 4;
    }

    private boolean isConfirmPasswordValid(String password,String confirmPassword) {

        return password.equals(confirmPassword);
    }

    private boolean isMobileNumberValid(String mobileNo) {

        return mobileNo.length() == 10;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegistrationActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegistrationTask extends AsyncTask<Void, Void, String> {

        private final String mName;
        private final String mEmail;
        private final String mPassword;
        private final String mMobile;

        UserRegistrationTask(String name,String email,String password, String mobile) {
            mName = name;
            mEmail = email;
            mPassword = password;
            mMobile = mobile;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                //send data to server to authenticate
                HttpClient httpClient = new DefaultHttpClient();
                //post header
                HttpPost httpPost = new HttpPost(postReceiverUrl);

                //add data
                List<NameValuePair>nameValuePairs =new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("firstName",mName));
                nameValuePairs.add(new BasicNameValuePair("email",mEmail));
                nameValuePairs.add(new BasicNameValuePair("password",mPassword));
                nameValuePairs.add(new BasicNameValuePair("mobile",mMobile));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //execute the post request
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity resEntity = httpResponse.getEntity();

                responseStr = EntityUtils.toString(resEntity).trim();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseStr;

        }

        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            Toast.makeText(getApplicationContext(), responseStr, Toast.LENGTH_LONG).show();
            Log.d("output", "Response : " + responseStr);
            mAuthTask = null;
            showProgress(false);

            if (responseStr.contains("Success")) {
                Log.d("output", "Response inside if : " + responseStr + " Sender's mobile no: " + mMobile);
                Intent i = new Intent(getApplicationContext(),WaitingForOtpActivity.class);

                //i.putExtra("RegId",pieces[1]);

                i.putExtra("phone",mMobile);
                startActivity(i);
                finish();
            } else {
                Log.d("output", "Response outside if : " + responseStr);
                if(responseStr.contains("Email")){
                    mEmailView.setError(getString(R.string.error_email_already_exit));
                    mEmailView.requestFocus();
                }
                else{
                    mMobileView.setError(getString(R.string.error_mobile_no_already_exist));
                    mMobileView.requestFocus();
                }

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }



}
