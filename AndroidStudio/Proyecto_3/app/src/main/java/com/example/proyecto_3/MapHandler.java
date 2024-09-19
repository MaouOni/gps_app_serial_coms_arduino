package com.example.proyecto_3;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHandler implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final Context context;
    private final TextView tvLatitude;
    private final TextView tvLongitude;
    private boolean userInteracted = false;

    public MapHandler(Context context, SupportMapFragment mapFragment, TextView tvLatitude, TextView tvLongitude) {
        this.context = context;
        this.tvLatitude = tvLatitude;
        this.tvLongitude = tvLongitude;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                userInteracted = true;
            }
        });

        mMap.setOnCameraIdleListener(() -> {
            if (userInteracted) {
                userInteracted = false;
            }
        });
    }

    public void updateMapLocation(LatLng location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(location).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            if (!userInteracted) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
        }
    }

    public void updateCoordinates(String latitude, String longitude) {
        tvLatitude.setText(context.getString(R.string.latitude_label, latitude));
        tvLongitude.setText(context.getString(R.string.longitude_label, longitude));
    }
}
