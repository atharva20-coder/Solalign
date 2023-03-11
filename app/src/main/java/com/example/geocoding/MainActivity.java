package com.example.geocoding;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView locationTextView, angleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        angleTextView = findViewById(R.id.angleTextView);
        TextInputEditText cityInputEditText = findViewById(R.id.cityInputEditText);
        ImageView searchIconImageView = findViewById(R.id.searchIconImageView);


        searchIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = cityInputEditText.getText().toString();

                if (locationName.isEmpty()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Please enter a city name.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                    if (addresses.size() > 0) {
                        double latitude = addresses.get(0).getLatitude();
                        double longitude = addresses.get(0).getLongitude();
                        String locationText = "Latitude: " + latitude + ", Longitude: " + longitude;
                        double panelAngle = calculateSolarPanelAngle(latitude, longitude);
                        locationTextView.setText(locationText);
                        angleTextView.setText("Optimal tilt angle for a solar panel at "+locationName+" is: "+panelAngle+"°");
                        Log.d(TAG, "Latitude: " + latitude);
                        Log.d(TAG, "Longitude: " + longitude);
                    } else {
                        Log.e(TAG, "Unable to geocode location: " + locationName);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error geocoding location", e);
                }
            }
        });
    }
    public double calculateSolarPanelAngle(double latitude, double longitude) {
        // Calculate the day of the year (1-365)
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        // Calculate the solar declination angle
        double solarDeclination = 23.45 * Math.sin(2 * Math.PI * (dayOfYear - 81) / 365);

        // Calculate the solar altitude angle
        double solarAltitude = Math.asin(Math.sin(Math.toRadians(latitude)) *
                Math.sin(Math.toRadians(solarDeclination)) +
                Math.cos(Math.toRadians(latitude)) *
                        Math.cos(Math.toRadians(solarDeclination)) *
                        Math.cos(Math.toRadians(longitude - 180)));

        // Calculate the solar zenith angle
        double solarZenith = Math.PI / 2 - solarAltitude;

        // Calculate the solar panel angle (assuming panel is tilted south and at latitude angle)
        double panelAngle = Math.toDegrees(Math.atan(Math.cos(solarZenith) /
                (Math.sin(Math.toRadians(latitude)) * Math.sin(solarZenith) -
                        Math.cos(Math.toRadians(latitude)) * Math.cos(solarZenith) *
                                Math.cos(Math.toRadians(180 - longitude)))));

        return panelAngle;
    }

}
