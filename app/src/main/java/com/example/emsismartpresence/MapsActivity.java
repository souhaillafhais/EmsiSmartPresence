package com.example.emsismartpresence;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.widget.Toast;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private Marker destinationMarker;
    private boolean firstUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        // Initialisation du client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialisation de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configuration de la carte
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Ajout du marqueur EMSI
        LatLng emsi = new LatLng(33.589331960959172, -7.049327980238995);
        mMap.addMarker(new MarkerOptions()
                .position(emsi)
                .title("EMSI Centre")
                .snippet("École Marocaine des Sciences de l'Ingénieur"));

        // Configuration des mises à jour de localisation
        setupLocationUpdates();

        // Gestion des clics sur la carte
        mMap.setOnMapClickListener(latLng -> {
            // Supprimer l'ancien marqueur de destination s'il existe
            if (destinationMarker != null) {
                destinationMarker.remove();
            }

            // Ajouter un nouveau marqueur
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Destination")
                    .snippet("Point sélectionné"));

            // Tracer l'itinéraire si la position utilisateur est disponible
            if (userMarker != null) {
                drawRoute(userMarker.getPosition(), latLng);
            }
        });
    }

    private void setupLocationUpdates() {
        // Vérification des permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Configuration de la requête de localisation
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Mise à jour toutes les 5 secondes
        locationRequest.setFastestInterval(2000); // Intervalle minimum
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Callback pour les mises à jour de position
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                }
            }
        };

        // Démarrer les mises à jour de position
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        // Obtenir la dernière position connue
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateLocationOnMap(location);
                    }
                });
    }

    private void updateLocationOnMap(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            // Premier marqueur
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Votre position")
                    .snippet("Position actuelle"));
            animateCamera(userLocation, 15f);
        } else {
            // Mise à jour de la position
            userMarker.setPosition(userLocation);
            if (firstUpdate) {
                animateCamera(userLocation, 15f);
                firstUpdate = false;
            }
        }
    }

    private void animateCamera(LatLng target, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(zoomLevel)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationUpdates();
            } else {
                // Gérer le refus de permission
                Toast.makeText(this, "La permission de localisation est nécessaire pour cette fonctionnalité", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates();
        }
    }
}