package com.example.gosu.wepathit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class PlacePickerActivity extends AppCompatActivity  {


    int PLACE_PICKER_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {

        //this.finishAffinity();
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
                        Intent intent = new Intent(getApplicationContext(),PlacePickerActivity.class);
                        startActivity(intent);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Activity mContext = PlacePickerActivity.this;
        new StartLocationAlert(mContext);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());

                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                LatLng userDestination = place.getLatLng();
                Double DUserDestLat = userDestination.latitude;
                String userDestLat = DUserDestLat.toString();
                Double DUserDestLong = userDestination.longitude;
                String userDestLong = DUserDestLong.toString();

                //passing value
                Intent myIntent = new Intent(this,MainActivity.class);
                myIntent.putExtra("DestinationName",place.getName());
                myIntent.putExtra("userDestLat",userDestLat);
                myIntent.putExtra("userDestLong",userDestLong);
                startActivity(myIntent);

            }
        }
    }

}