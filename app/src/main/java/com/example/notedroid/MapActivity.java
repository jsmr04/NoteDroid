package com.example.notedroid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.example.notedroid.model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "NoteDroid";
    private GoogleMap mapAPI;
    private SupportMapFragment mapFragment;
    private ArrayList<Location> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Activando boton back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            locations = (ArrayList<Location>) bundle.getSerializable("LOCATIONS");
        }

        for (Location l : locations){
            Log.d(TAG, "Location: " + l.getLocation());
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_Fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;
        ArrayList<LatLng> latLngs = new ArrayList<>();
        int index = 0;

        for (Location location: locations) {
            if (location.getLocation().contains(",")) {
                double latitude = Double.parseDouble(location.getLocation().split(",")[0]);
                double longitude = Double.parseDouble(location.getLocation().split(",")[1]);

                latLngs.add(new LatLng(latitude, longitude));
            }
        }

        if (latLngs.size() > 0) {
            CameraPosition.Builder destBuilder = new CameraPosition.Builder();
            CameraPosition dest = destBuilder.target(latLngs.get(0))
                    .zoom(12f)
                    .build();
            mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(dest));
        }

        for (LatLng latLng : latLngs){
            mapAPI.addMarker(new MarkerOptions().position(latLng));
            if (index > 0) {
                Polyline line = mapAPI.addPolyline(new PolylineOptions()
                        .add(latLngs.get(index - 1), latLng)
                        .width(10)
                        .color(Color.RED));
            }
            index++;
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}