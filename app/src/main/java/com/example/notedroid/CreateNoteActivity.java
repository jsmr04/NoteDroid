package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.notedroid.Adapter.ImageAdapter;
import com.example.notedroid.model.Media;
import com.example.notedroid.model.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateNoteActivity extends AppCompatActivity {
    public static final int NOTE_MODE_CREATE = 0;
    public static final int NOTE_MODE_EDIT = 1;
    private static final String TAG = "NoteDroid";
    private static final int REQUEST_CODE = 10;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 20;
    private static final int REQUEST_IMAGE_CAPTURE = 30;
    private static final int REQUEST_PHOTO_LIBRARY = 40;
    private static final int REQUEST_TAKE_PHOTO = 1;
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
    private DatabaseReference refMedia;
    private Note note;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<Media> medias = new ArrayList<>();
    private ArrayList<String> removedImages = new ArrayList<>();
    private String category = "No Category";
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private boolean recording = false;
    private String base64Audio = "";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Bitmap> images = new ArrayList<>();
    private ArrayList<com.example.notedroid.model.Location> locations = new ArrayList<>();
    private int mode = -1;
    private String editNoteId = "";

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToCamera = false;

    private String[] audioPermissions = {Manifest.permission.RECORD_AUDIO};
    private String[] imagePermissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        setTitle("");

        database = FirebaseDatabase.getInstance();
        refNote = database.getReference("note");
        refLocation = database.getReference("location");
        refMedia = database.getReference("media");

        titleTextView = findViewById(R.id.cNoteTitleTextView);
        noteTextView = findViewById(R.id.cNoteNoteTextView);
        categoryTextView = findViewById(R.id.cNoteCategoryTextView);
        playImageView = findViewById(R.id.cNotePlayImageView);
        mapImageView = findViewById(R.id.cNoteMapImageView);
        recyclerView = findViewById(R.id.images_recyclerView);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(images);
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_save_36);

        categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectCategory();
            }
        });

        setupRecyclerView();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("NOTE")) {
                note = (Note) bundle.getSerializable("NOTE");
                Log.d(TAG, "onCreate NOTE: " + note.getTitle());
            }

            if (bundle.containsKey("MODE")) {
                mode = bundle.getInt("MODE");
                Log.d(TAG, "onCreate MODE: " + mode);
            }
        }

        if (mode == NOTE_MODE_EDIT) {
            fillFields();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mode == NOTE_MODE_CREATE) {
            getCurrentLocation();
        }
    }

    private void fillFields() {
        if (note != null) {
            titleTextView.setText(note.getTitle());
            noteTextView.setText(note.getNote());
            categoryTextView.setText(note.getCategory());
        }

        //Getting media
        Query mediaQuery = refMedia;
        Log.d(TAG, "note id: " + note.getId());
        mediaQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medias.clear();
                images.clear();
                for (DataSnapshot mediaSnapshot : snapshot.getChildren()) {
                    Media media = mediaSnapshot.getValue(Media.class);

                    if (media != null && media.getNoteId().equals(note.getId())) {
                        medias.add(media);
                        if (media.getType().equals("IMAGE")) {
                            images.add(Util.stringToBitMap(media.getMedia()));
                        }

                        Log.d(TAG, "onDataChange: " + media.getId());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });

        //Getting Location
        Query locationQuery = refLocation;
        Log.d(TAG, "note id: " + note.getId());
        locationQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locations.clear();
                for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    com.example.notedroid.model.Location location
                            = locationSnapshot.getValue(com.example.notedroid.model.Location.class);

                    if (location != null && location.getNoteId().equals(note.getId())) {
                        locations.add(location);
                        Log.d(TAG, "onDataChange: " + location.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });

//        mediaQuery.
//        refMedia.addValueEventListener(noteListener);

    }

    private void setupRecyclerView() {
        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        recyclerView.setLayoutAnimation(animation);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d(TAG, "onSwiped: " + viewHolder.getAdapterPosition());
                removeImage(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showImage(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
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
        finish();

        return true;
    }

    private void removeImage(int index) {
        //Delete image
        images.remove(index);
        int i = 0;
        int removeIndex = 0;
        boolean delete = false;

        //Delete image from media
        for (Media m : medias) {
            if (m.getType().equals("IMAGE")) {
                if (i == index) {
                    delete = true;
                    break;
                }
                i++;
            }
            removeIndex++;
        }

        if (delete) {
            //Getting id
            if (!medias.get(removeIndex).getId().equals("")) {
                Log.d(TAG, "removeImage: " + medias.get(removeIndex).getType());
                Log.d(TAG, "removeImage: " + medias.get(removeIndex).getId());
                removedImages.add(medias.get(removeIndex).getId());
            }
            medias.remove(removeIndex);
        }
        adapter.notifyDataSetChanged();
    }

    private void createNoteInFirebase() {
        String title = titleTextView.getText().toString();
        String noteText = noteTextView.getText().toString();
        String category = categoryTextView.getText().toString();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());

        if (title == "") {
            title = "Untitled";
        }

        if (noteText != "") {
            //Create Note
            String key = "";
            if (mode == NOTE_MODE_EDIT) {
                if (note != null) {
                    key = note.getId();
                }
            } else if (mode == NOTE_MODE_CREATE) {
                key = refNote.child("note").push().getKey();
            }
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
                    deleteMedia();
                    storeMedia();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure");
                    Toast.makeText(getApplicationContext(), "Error saving the note", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void storeMedia() {
        for (Media media : medias) {
            //Add new media
            //Relation to note
            if (media.getId().equals("") || media.getType().equals("AUDIO")) {
                media.setNoteId(note.getId());
                if (mode == NOTE_MODE_CREATE || media.getId().equals("")) {
                    media.setId(refMedia.child("media").push().getKey());
                }

                Log.d(TAG, "storeMedia: " + media.getId());
                refMedia.child(media.getId()).setValue(media).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess");
                        Toast.makeText(getApplicationContext(), "Media saved successfully", Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure");
                    }
                });
            }
        }

    }

    private void deleteMedia(){
        //TODO: Add delete media
        for (final String mediaId : removedImages) {
            refMedia.child(mediaId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess - Remove" + mediaId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure");
                }
            });
        }
    }

    private void storeLocation() {
        for (com.example.notedroid.model.Location loc : locations) {
            if (loc.getId().equals("")) {
                loc.setId(refLocation.child("location").push().getKey());
                loc.setNoteId(note.getId());

                    loc.setNoteId(note.getId());
                    refLocation.child(loc.getId()).setValue(loc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess");
                            Toast.makeText(getApplicationContext(), "Location saved successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure");
                        }
                    });
            }
        }
    }

    private void getCurrentLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Toast.makeText(getApplicationContext(), "New location detected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onLocationChanged - Lat: " + currentLocation.getLatitude());
                Log.d(TAG, "onLocationChanged - Lon: " + currentLocation.getLongitude());

                com.example.notedroid.model.Location currentLocation;
                currentLocation = new com.example.notedroid.model.Location();

                currentLocation.setId("");
                currentLocation.setNoteId("");
                currentLocation.setLocation(CreateNoteActivity.this.currentLocation.getLatitude() + "," + CreateNoteActivity.this.currentLocation.getLongitude());
                locations.add(currentLocation);

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
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            showImagePicker();

            //
        }

    }

    private void setupAudioRecorder() {
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audio.3gp";
        ActivityCompat.requestPermissions(this, audioPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void setupCamera() {
        Log.d(TAG, "setupCamera: CAMERA: " + checkSelfPermission(Manifest.permission.CAMERA));
        Log.d(TAG, "setupCamera: WRITE_EXTERNAL_STORAGE: " + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, imagePermissions, REQUEST_IMAGE_CAPTURE);
        } else {
            permissionToCamera = true;
        }

    }

    private void showSelectCategory() {

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

        for (String cat : Util.getPreferences(this, "CAT_")) {
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

    private void showAddCategory() {
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
                Util.setPreference(getApplicationContext(), "CAT_" + addNewCatTextView.getText().toString().trim(), addNewCatTextView.getText().toString());
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(addCategoryLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Log.d(TAG, "onOptionsItemSelected: ");
        //boolean result = false;
        switch (item.getItemId()) {
            case R.id.createNote_attachments:
                showAttachments();
                return true;
            case R.id.createNote_share:
                shareNote();
                return true;
            case  R.id.createNote_save:
                createNoteInFirebase();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareNote(){
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);

        //Creating content to share
        String shareBody = "Title: " + titleTextView.getText().toString() + "\n"
                + "Category: " + categoryTextView.getText().toString() + "\n"
                + noteTextView.getText().toString();

        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "iNote - " + titleTextView.getText().toString() );
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(intent, "Choose"));
    }

    private void showImagePicker() {
        if (permissionToCamera) {
            Intent takePicture = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //takePicture.setType("image/*");
            startActivityForResult(takePicture, REQUEST_PHOTO_LIBRARY);
        }
    }

    private void showAudioRecorder() {
        permissionToRecordAccepted = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (permissionToRecordAccepted) {
            ImageView playStopImageView;
            final ImageView addAudioImageView;
            final ImageView recordingGIFImageView;
            final ImageView closeImageView;

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            View recorderLayout = LayoutInflater.from(this).inflate(R.layout.audio_recorder, null);

            playStopImageView = recorderLayout.findViewById(R.id.playStop_ImageView);
            addAudioImageView = recorderLayout.findViewById(R.id.addAudio_ImageView);
            recordingGIFImageView = recorderLayout.findViewById(R.id.recording_ImageView);
            closeImageView = recorderLayout.findViewById(R.id.recorderClose_ImageView);

            addAudioImageView.setVisibility(View.INVISIBLE);
            recordingGIFImageView.setVisibility(View.INVISIBLE);

            playStopImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recording) {
                        recording = false;
                    } else {
                        recording = true;
                    }

                    if (recording) {
                        startRecorder();
                        recordingGIFImageView.setVisibility(View.VISIBLE);
                    } else {
                        stopRecorder();
                        recordingGIFImageView.setVisibility(View.INVISIBLE);
                        addAudioImageView.setVisibility(View.VISIBLE);
                    }
                    changeRecorderImage(v);
                }
            });

            addAudioImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Path path = Paths.get(fileName);
                    base64Audio = Util.fileToString(path);
                    boolean audioExists = false;
                    int mediaIndex = 0;
                    Log.d(TAG, "onClick: " + base64Audio);

                    mediaIndex = getAudioIndex();

                    if (mediaIndex >= 0) {
                        //Editing existing audio
                        medias.get(mediaIndex).setMedia(base64Audio);
                    } else {
                        //Add new audio
                        Media media = new Media();

                        media.setId("");
                        media.setType("AUDIO");
                        media.setMedia(base64Audio);
                        media.setNoteId("");

                        medias.add(media);
                    }

                    alertDialog.dismiss();
                }
            });

            closeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.setView(recorderLayout);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    private void startRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecorder() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void changeRecorderImage(View v) {
        ImageView imageView = (ImageView) v;
        if (recording) {
            imageView.setImageResource(R.drawable.ic_baseline_stop_48);
        } else {
            imageView.setImageResource((R.drawable.ic_baseline_play_arrow_48));
        }
    }

    public int getAudioIndex() {
        int mediaIndex = 0;
        for (Media media : medias) {
            if (media.getType().equals("AUDIO")) {
                return mediaIndex;
            }
            mediaIndex++;
        }
        return -1;
    }

    public void playAudio(View v) {
        player = new MediaPlayer();
        try {
            int mediaIndex = getAudioIndex();

            if (mediaIndex >= 0) {
                Log.d(TAG, "playAudio: " + mediaIndex);
                String audioFileName = getExternalCacheDir().getAbsolutePath() + "/audioToPlay.3gp";
                File audio = Util.stringToFile(medias.get(mediaIndex).getMedia(), audioFileName);
                player.setDataSource(audio.getPath());
                player.prepare();
                player.start();
            }
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            try {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                addImage(imageBitmap);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_PHOTO_LIBRARY && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    addImage(imageBitmap);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addImage(Bitmap bitmap) {
        images.add(bitmap);

        //Add new image
        String base64Image = Util.bitMapToString(bitmap);

        Media media = new Media();

        media.setId("");
        media.setType("IMAGE");
        media.setMedia(base64Image);
        media.setNoteId("");
        medias.add(media);
    }

    private void dispatchTakePictureIntent() {
        //showImagePicker
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void showPhotoLibrary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_PHOTO_LIBRARY);
        }
    }

    private void showAttachments() {
        TextView recordTextView;
        TextView libraryTextView;
        TextView takePhotoTextView;
        TextView locationTextView;

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View attachmentLayout = LayoutInflater.from(this).inflate(R.layout.attachments_picker_layout, null);

        recordTextView = attachmentLayout.findViewById(R.id.attachRecord_TextView);
        libraryTextView = attachmentLayout.findViewById(R.id.attachLibrary_TextView);
        takePhotoTextView = attachmentLayout.findViewById(R.id.attachTakePhoto_TextView);
        locationTextView = attachmentLayout.findViewById(R.id.attachLocation_TextView);

        recordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                setupAudioRecorder();
                showAudioRecorder();
            }
        });

        libraryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                showPhotoLibrary();
            }
        });

        takePhotoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                setupCamera();
                dispatchTakePictureIntent();
            }
        });

        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = "Do you want to get a new location?";
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getCurrentLocation();
                    }
                };

                alertDialog.dismiss();
                showMessage("Location", message, "Yes", onClickListener);
            }
        });

        alertDialog.setView(attachmentLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public void showMessage(String title, String message, String positiveText, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(positiveText, onClickListener)
                .create()
                .show();
    }

    public void showImage(int index) {
        ImageView imageView;

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View fullImageLayout = LayoutInflater.from(this).inflate(R.layout.full_image_layout, null);

        imageView = fullImageLayout.findViewById(R.id.full_ImageView);
        imageView.setImageBitmap(images.get(index));

        alertDialog.setView(fullImageLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("LOCATIONS", locations);
        startActivity(intent);
    }
}