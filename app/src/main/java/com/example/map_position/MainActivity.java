package com.dali.map_position;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;

import android.Manifest;
import android.os.Looper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;  // Renommé GoogleMap en mMap pour éviter les conflits de noms
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file as the content view.
        setContentView(R.layout.activity_main);

        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Créer une requête de localisation
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Intervalle de mise à jour (5 secondes)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Définir le callback pour la localisation
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Assigner la variable globale mMap
        mMap = googleMap;

        // Ajouter un marqueur de base à une position initiale
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(34, 10))
                .title("Marker"));

        // Vérifier si la permission est accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            // Demander la permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void startLocationUpdates() {
        // Vérifier si la permission est accordée avant de démarrer les mises à jour de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void updateLocation(Location location) {
        if (location != null) {
            // Obtenir les coordonnées et afficher sur la carte
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (currentLocationMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Position actuelle");
                currentLocationMarker = mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                currentLocationMarker.setPosition(latLng);
            }

            // Calculer la vitesse et afficher en km/h
            float speedInKmh = (location.getSpeed() * 3600) / 1000; // Conversion de m/s en km/h
            Toast.makeText(this, "Vitesse : " + speedInKmh + " km/h", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Arrêter les mises à jour de localisation lorsque l'activité est en pause
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reprendre les mises à jour de localisation
        startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, démarrer les mises à jour de localisation
                startLocationUpdates();
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
