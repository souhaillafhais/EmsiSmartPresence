package com.example.emsismartpresence;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline currentPolyline;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Bouton de localisation
        Button locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerMapOnMyLocation();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Ajout des marqueurs EMSI
        LatLng emsiRoudani = new LatLng(33.5849, -7.6244);
        LatLng emsiCentre1 = new LatLng(33.5895, -7.6120);
        LatLng emsiCentre2 = new LatLng(33.5890, -7.6115);
        LatLng emsiOrangers = new LatLng(33.5735, -7.6200);
        LatLng emsiMoulayYoussef = new LatLng(33.5890, -7.6200);
        LatLng emsiCFC = new LatLng(33.5660, -7.6200);

        mMap.addMarker(new MarkerOptions().position(emsiRoudani).title("EMSI Roudani"));
        mMap.addMarker(new MarkerOptions().position(emsiCentre1).title("EMSI Centre"));
        mMap.addMarker(new MarkerOptions().position(emsiCentre2).title("EMSI Centre 2"));
        mMap.addMarker(new MarkerOptions().position(emsiOrangers).title("EMSI Les Orangers"));
        mMap.addMarker(new MarkerOptions().position(emsiMoulayYoussef).title("EMSI Moulay Youssef"));
        mMap.addMarker(new MarkerOptions().position(emsiCFC).title("EMSI Casa Finance City"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(emsiCentre1, 12));

        // Gestion des permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Gestion du clic sur les marqueurs
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                drawRouteToMarker(marker);
                return true;
            }
        });
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            centerMapOnMyLocation();
        }
    }

    private void centerMapOnMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
            } else {
                Toast.makeText(this, "Impossible de récupérer votre position", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void drawRouteToMarker(Marker marker) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng destination = marker.getPosition();

                // Supprimer l'ancienne polyligne si elle existe
                if (currentPolyline != null) {
                    currentPolyline.remove();
                }

                // Ajouter la nouvelle polyligne
                currentPolyline = mMap.addPolyline(new PolylineOptions()
                        .add(current, destination)
                        .width(8)
                        .color(Color.BLUE)
                        .geodesic(true));

                // Centrer la carte sur l'itinéraire
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        new LatLngBounds.Builder()
                                .include(current)
                                .include(destination)
                                .build(), 100));
            } else {
                Toast.makeText(this, "Position actuelle non disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}