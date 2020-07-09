package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notedroid.model.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateNoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteDroid";
    private static final int REQUEST_CODE = 10;
    private final long MIN_TIME = 1000;
    private final float MIN_DISTANCE = 10;
    private TextView titleTextView;
    private TextView noteTextView;
    private TextView categoryTextView;
    private ImageView playImageView;
    private ImageView mapImageView;
    private FirebaseDatabase database;
    private DatabaseReference refNote;
    private DatabaseReference refLocation;
    private Note note;
    private com.example.notedroid.model.Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private ArrayList<String> items = new ArrayList<>();
    private String category = "No Category";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        setTitle("");

        database = FirebaseDatabase.getInstance();
        refNote = database.getReference("note");
        refLocation = database.getReference("location");

        titleTextView = findViewById(R.id.cNoteTitleTextView);
        noteTextView = findViewById(R.id.cNoteNoteTextView);
        categoryTextView = findViewById(R.id.cNoteCategoryTextView);
        playImageView = findViewById(R.id.cNotePlayImageView);
        mapImageView = findViewById(R.id.cNoteMapImageView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_save_36);

        categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectCategory();
            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getCurrentLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_note_menu, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        createNoteInFirebase();

        return true;
    }

    private void createNoteInFirebase(){
        String title = titleTextView.getText().toString();
        String noteText = noteTextView.getText().toString();
        String category = categoryTextView.getText().toString();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());

        if (title == ""){
            title = "Untitled";
        }

        if (noteText != "") {
            //Create Note
            String key = refNote.child("note").push().getKey();
            Log.d(TAG, "KEY: " + key);

            note = new Note();
            note.setId(key);
            note.setTitle(title);
            note.setNote(noteText);
            note.setCategory(category);
            note.setNoteDate(date);
            note.setUser(Util.getPreference(this, "user"));

            refNote.child(key).setValue(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess");
                    storeLocation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure");
                    Toast.makeText(getApplicationContext(), "Error saving the note",Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    private void storeLocation(){
        String key = refLocation.child("location").push().getKey();
        location = new com.example.notedroid.model.Location();
        location.setId(key);
        location.setNoteId(note.getId());
        location.setLocation(currentLocation.getLatitude() + "," + currentLocation.getLongitude());

        refLocation.child(key).setValue(location).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess");
                Toast.makeText(getApplicationContext(), "Note saved successfully",Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure");
            }
        });

    }

    private void getCurrentLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Toast.makeText(getApplicationContext(), "New location detected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onLocationChanged - Lat: " + currentLocation.getLatitude());
                Log.d(TAG, "onLocationChanged - Lon: " + currentLocation.getLongitude());

                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
            return;
        }

        //Getting location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Log.d(TAG, "Location access granted");
                getCurrentLocation();
            } else {
                Log.d(TAG, "Location access denied");
            }

        }
    }

    private void showSelectCategory(){

        TextView newCategoryTextView;
        ImageView addCategory;
        final Spinner categorySpinner;

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View categoryLayout = LayoutInflater.from(this).inflate(R.layout.category_layout, null);

        newCategoryTextView = categoryLayout.findViewById(R.id.categoryNewTextView);
        categorySpinner = categoryLayout.findViewById(R.id.categorySpinner);
        addCategory = categoryLayout.findViewById(R.id.addCatImageView);

        //Lista de opciones
        items.clear();
        items.add("No Category");
        items.add("School");
        items.add("Recipe");
        items.add("Gym");

        for ( String cat:Util.getPreferences(this, "CAT_")){
            items.add(cat);
        }

        //Adapter del spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.category_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        //Select new category
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = categorySpinner.getSelectedItem().toString();
                categoryTextView.setText(category);
                alertDialog.dismiss();
            }
        });

        //Add new category
        newCategoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategory();
            }
        });

        alertDialog.setView(categoryLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void showAddCategory(){
        final TextView addNewCatTextView;
        ImageView addImageView;

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View addCategoryLayout = LayoutInflater.from(this).inflate(R.layout.add_category_layout, null);

        addNewCatTextView = addCategoryLayout.findViewById(R.id.addCatTextView);
        addImageView = addCategoryLayout.findViewById(R.id.addCatImageView);

        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add(addNewCatTextView.getText().toString());
                Util.setPreference(getApplicationContext(), "CAT_" + addNewCatTextView.getText().toString().trim(),addNewCatTextView.getText().toString());
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(addCategoryLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}