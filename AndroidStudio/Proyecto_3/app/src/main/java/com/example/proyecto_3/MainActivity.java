package com.example.proyecto_3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, UsbCommunication.UsbCommunicationListener {

    private UsbCommunication usbCommunication;
    private TextView tvLatitude, tvLongitude;
    private ProgressBar loadingIndicator;
    private MapHandler mapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Set TextViews to invisible initially
        tvLatitude.setVisibility(View.INVISIBLE);
        tvLongitude.setVisibility(View.INVISIBLE);

        usbCommunication = new UsbCommunication(this, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usbCommunication.initialize();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapHandler = new MapHandler(this, mapFragment, tvLatitude, tvLongitude);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // This is handled by the MapHandler now
    }

    @Override
    public void onUsbDataReceived(String data) {
        runOnUiThread(() -> {
            Log.d("MainActivity", "Received USB Data: " + data);
            String[] gpsData = data.split(",");
            if (gpsData.length >= 2) {
                try {
                    double latitude = Double.parseDouble(gpsData[0]);
                    double longitude = Double.parseDouble(gpsData[1]);
                    LatLng currentLocation = new LatLng(latitude, longitude);

                    mapHandler.updateCoordinates(gpsData[0], gpsData[1]);
                    mapHandler.updateMapLocation(currentLocation);
                    loadingIndicator.setVisibility(View.GONE);

                    // Set TextViews to visible when data is received
                    tvLatitude.setVisibility(View.VISIBLE);
                    tvLongitude.setVisibility(View.VISIBLE);
                } catch (NumberFormatException e) {
                    Log.d("MainActivity", "Bad Format Data: " + data);
                }
            } else {
                Log.e("MainActivity", "Incomplete GPS data received: " + data);
            }
        });
    }

    @Override
    public void onUsbPermissionDenied() {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Permiso Denegado Automáticamente")
                .setMessage("Permiso denegado para dispositivos USB. La aplicación se reiniciará para acceder con él.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> restartApp())
                .setCancelable(false)
                .show());
    }

    @Override
    public void onUsbConnected() {
        runOnUiThread(() -> loadingIndicator.setVisibility(View.VISIBLE));
    }

    @Override
    public void onUsbDeviceNotFound() {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Nigún Dispositivo USB Conectado")
                .setMessage("No se encontró ningún dispositivo USB. Por favor, conecte su Arduino y reinicie la aplicación.")
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show());
    }

    private void restartApp() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }, 2000); // Wait for 2 seconds before restarting
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbCommunication.close();
    }
}
