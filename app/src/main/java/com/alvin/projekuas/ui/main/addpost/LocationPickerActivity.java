package com.alvin.projekuas.ui.main.addpost;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alvin.projekuas.R;
import com.alvin.projekuas.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private ActivityLocationPickerBinding binding;

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng startLat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pilih Lokasi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        statusCheck();

        binding.btnSaveLocation.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        googleMap.clear(); //clear old markers

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        fetchLastLocation(mMap);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    LatLng center = mMap.getCameraPosition().target;
                    latitude = center.latitude;
                    longitude = center.longitude;

                    binding.tvAlamat.setText(getStringAddress(center.latitude, center.longitude));
                } catch (Exception e) {
                    Toast.makeText(LocationPickerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchLastLocation(final GoogleMap mMap) {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    startLat = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    CameraPosition googlePlex = CameraPosition.builder()
                            .target(startLat)
                            .zoom(17)
                            .bearing(0)
                            .build();

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);

                }
            }
        });

    }

    public String getStringAddress(Double lat, Double lng) {
        String address = "";
        String city = "";

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address + " " + city;

    }

    private void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS anda tidak menyala, apakah ingin menyalakan GPS?")
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_location:
                String address = getStringAddress(latitude, longitude);
                setResult(Activity.RESULT_OK, new Intent().putExtra("latitude", latitude)
                        .putExtra("longitude", longitude)
                        .putExtra("alamat", address));
                finish();
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
