package com.example.geocoding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView locationTextView, angleTextView, cityTV;


    String panelAngle;

    String cityName="Turn On \uD83D\uDCCD\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        cityTV = findViewById(R.id.CityTV);
        angleTextView = findViewById(R.id.angleTextView);
        TextInputEditText cityInputEditText = findViewById(R.id.cityInputEditText);
        ImageView searchIconImageView = findViewById(R.id.searchIconImageView);
        Button btnShare = findViewById(R.id.btn_share);
        Button btnDocumentation = findViewById(R.id.btn_documentation);


        checkLocationPermission();
        getUserLocation();


        searchIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = cityInputEditText.getText().toString();

                if (locationName.isEmpty()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Please enter a valid city name.")
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
                        panelAngle = calculateSolarPanelAngleAndDirection(latitude, longitude);
                        locationTextView.setText(locationText);
                        angleTextView.setText(panelAngle);
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

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareContent();
            }
        });

        btnDocumentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Documentation");
                builder.setMessage("Tilt & Azimuth Angle: What Angle Should I Tilt My Solar Panels?\n" +
                        "The “tilt angle” or “elevation angle” describes the vertical angle of your solar panels. “Azimuth angle” is their horizontal facing in relation to the Equator.\n" +
                        "\n" +
                        "Solar panels should face directly into the sun to optimize their output. This article explains how to find the right tilt and azimuth angle to get the most production out of your array.\n" +
                        "\n" +
                        "Welcome to another entry in our ongoing Solar 101 series. Today we’re going to explain how to mount your solar panels to get the most energy from them.\n" +
                        "\n" +
                        "Let’s start with two key terms: elevation angle and azimuth angle (commonly shortened to “angle” and “azimuth” for brevity).\n" +
                        "\n" +
                        "Elevation Angle: The vertical tilt of your panels.\n" +
                        "Azimuth Angle: The horizontal orientation of your panels (in relation to the equator, in this case)." +
                        "Solar panels work best when they face directly into the sun. But that task is complicated by the fact that the sun moves across the sky throughout the day. It also changes angle in the sky as the seasons change.\n" +
                        "\n" +
                        "So when you build a solar system, the question is: what’s the best angle to mount your solar panels to get the most output?");

                // Add a "Read more" button
                builder.setPositiveButton("Read more", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open a web link
                        String url = "https://unboundsolar.com/blog/solar-panel-azimuth-angle";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });
    }
    public String calculateSolarPanelAngleAndDirection(double latitude, double longitude) {
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

        // Calculate the direction
        String direction = "";
        if (longitude == 0) {
            if (latitude > 0) {
                direction = "North";
            } else if (latitude < 0) {
                direction = "South";
            }
        } else if (latitude == 0) {
            if (longitude > 0) {
                direction = "East";
            } else if (longitude < 0) {
                direction = "West";
            }
        } else if (longitude > 0 && latitude > 0) {
            direction = "North East";
        } else if (longitude < 0 && latitude > 0) {
            direction = "North West";
        } else if (longitude > 0 && latitude < 0) {
            direction = "South East";
        } else if (longitude < 0 && latitude < 0) {
            direction = "South West";
        }

        // Return the result as a string
                return "Solar Panel at Angle: " + String.format("%.2f", panelAngle) + " degrees\nDirection of Solar Panel: " + direction;
    }

        private static final int PERMISSION_REQUEST_LOCATION = 1;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }
    private FusedLocationProviderClient fusedLocationClient;

    private void getUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            // Permission is already granted, access the location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            cityName = addresses.get(0).getLocality();
                            Log.d("TAG", "City: " + cityName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    cityTV.setText(cityName);
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }

    private void shareContent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Content Subject");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Calculated with solAlign made by Atharva"+"\n"+panelAngle);

        startActivity(Intent.createChooser(shareIntent, "Share Content Using"));
    }

}
