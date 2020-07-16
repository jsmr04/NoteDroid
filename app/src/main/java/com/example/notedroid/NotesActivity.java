package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.example.notedroid.Adapter.CategoryAdapter;
import com.example.notedroid.Adapter.CategoryViewHolder;
import com.example.notedroid.Adapter.NoteViewHolder;
import com.example.notedroid.Interface.NoteOnClickInterface;
import com.example.notedroid.model.CategoryNotes;
import com.example.notedroid.model.Location;
import com.example.notedroid.model.Media;
import com.example.notedroid.model.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.zip.Inflater;

public class NotesActivity extends AppCompatActivity{
    private static final String TAG = "NoteDroid";
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SearchView noteSearchView;
    private RecyclerView.Adapter adapter, adapterNotes;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference refNote, refMedia, refLocation;
    private ArrayList<Note> allNotes = new ArrayList<>();
    private FirebaseDatabase database;
    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<Media> medias = new ArrayList<>();
    private ArrayList<Location> locations = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<CategoryNotes> categoryNotes = new ArrayList<>();
    private ArrayList<Date> datesDescending = new ArrayList<>();
    private String typeSorting = "id";
    private String user = "";
    private Context context;
    private FirebaseAuth mAuth;
    private int selected = 0;
    private boolean longSelected = false;
    private Note noteForDeletion;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        fab = findViewById(R.id.addNoteFAB);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.notes_recyclerview);
        progressBar = findViewById(R.id.notes_progressBar);
        noteSearchView = findViewById(R.id.note_SearchView);

        user = Util.getPreference(this, "user");
        Log.d(TAG, "onCreate User: " + user);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        refNote = database.getReference("note");
        refMedia = database.getReference("media");
        refLocation = database.getReference("location");

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        //Log.d("Cat", this.notes.get(0).toString());

        setupRecyclerView();

        getNotesFromFirebase("id");
        getLocationsFromFirebase();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            setTitle("");
            //Activando boton back
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewNote();
            }
        });

        Log.d(TAG, "onCreate: Preparing search");
        EditText editText = noteSearchView.findViewById(R.id.search_src_text);
        editText.setHintTextColor(Color.WHITE);
        editText.setTextColor(Color.WHITE);

        noteSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);

                notes.clear();
                for (Note n : allNotes) {
                    Note note = new Note();
                    note.setId(n.getId());
                    note.setTitle(n.getTitle());
                    note.setCategory(n.getCategory());
                    note.setNote(n.getNote());
                    note.setNoteDate(n.getNoteDate());
                    note.setUser(n.getUser());

                    if (newText.isEmpty()) {
                        datesDescending.add(note.getNoteDateToDate());
                        notes.add(note);
                    } else if (n.getTitle().toUpperCase().contains(newText.toUpperCase())
                            || n.getNote().toUpperCase().contains(newText.toUpperCase())) {
                        datesDescending.add(note.getNoteDateToDate());
                        notes.add(note);
                    }
                }

                Log.d(TAG, "onQueryTextChange notes.size: " + notes.size());
                Log.d(TAG, "onQueryTextChange allNotes.size: " + allNotes.size());

                typeSorting = "id";
                startCategoryNotes();

                adapter.notifyDataSetChanged();
                if (adapterNotes != null) {
                    adapterNotes.notifyDataSetChanged();
                }
                return false;
            }
        });

    }

    private void setupRecyclerView(){
        adapter = new RecyclerView.Adapter<CategoryViewHolder>() {
            @Override
            public void onBindViewHolder(@NonNull CategoryViewHolder categoryViewHolder, final int positionCategory) {
                categoryViewHolder.categoryName.setText(categoryNotes.get(positionCategory).getCategoryName());

                Log.d("media","mediaCategoryName: " + categoryNotes.get(positionCategory).getCategoryName());
                Log.d("media","mediaCategoryPosition: " + positionCategory);

                adapterNotes = new RecyclerView.Adapter<NoteViewHolder>() {
                    private ArrayList<Note> notes = categoryNotes.get(positionCategory).getNotes();
                    @Override
                    public void onBindViewHolder(@NonNull final NoteViewHolder noteViewHolder,  int positionNote) {
                        String image = null;
                        Log.d("media", "mediaNote: " +  medias.size());
                        Log.d("media", "mediaNotePosition: " + positionNote);
                        Log.d(TAG, "media: " + notes.get(positionNote).getTitle());

                        for (Media m : medias){
                            if (m.getType().equals("IMAGE") && m.getNoteId().equals(notes.get(positionNote).getId())){
                                Log.d("media", "mediaNoteViewHolderin: " + m.getMedia());
                                //image = Util.stringToBitMap(m.getMedia());
                                image = m.getMedia();
                            }
                            Log.d("media", "mediaNoteViewHolderout: " + m.getNoteId() + " NoteID: " + notes.get(positionNote).getId() + " MediaType: " + m.getType());
                        }
                        if (image == null){
                            noteViewHolder.noteImageView.setImageResource(R.mipmap.note_img);
                        }else{
                            Bitmap bit;
                            bit = Util.stringToBitMap(image);
                            noteViewHolder.noteImageView.setImageBitmap(bit);
                        }
                        noteViewHolder.titleTextView.setText(notes.get(positionNote).getTitle());
                        noteViewHolder.dateTextView.setText(notes.get(positionNote).getNoteDate());
                        final int positionN = positionNote;
                        noteViewHolder.NoteInterfaceClick(new NoteOnClickInterface() {
                            @Override
                            public void onClick(View view, boolean isLongPressed) {
                                Log.d(TAG, "onLongClick: OnClick " + isLongPressed);
                                if (!isLongPressed && !longSelected){
                                    Intent intent = new Intent(NotesActivity.this, CreateNoteActivity.class);
                                    intent.putExtra("NOTE", notes.get(positionN));
                                    intent.putExtra("MODE", CreateNoteActivity.NOTE_MODE_EDIT);
                                    startActivity(intent);
                                }else{
                                    longSelected = false;
                                }
                            }

                            @Override
                            public void onLongClick(View view, boolean isLongPressed) {
                                Log.d(TAG, "onLongClick: NotesActivity :" + isLongPressed);
                                if(isLongPressed){
                                    if(selected == 0) {
                                        noteViewHolder.titleTextView.setTextColor(Color.RED);
                                        noteViewHolder.dateTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                                        noteViewHolder.noteView.setVisibility(View.INVISIBLE);
                                        noteViewHolder.deleteIcon.setVisibility(View.VISIBLE);
                                        noteViewHolder.deleteIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d(TAG, "onClick: DeleteButtonPressed");
                                                deleteNote(notes.get(positionN).getId());
                                            }
                                        });
                                        //noteForDeletion = notes.get(positionN);
                                        selected = 1;
                                    }
                                }else{
                                    noteViewHolder.titleTextView.setTextColor(Color.WHITE);
                                    noteViewHolder.dateTextView.setTextColor(Color.WHITE);
                                    noteViewHolder.noteView.setVisibility(View.VISIBLE);
                                    noteViewHolder.deleteIcon.setVisibility(View.INVISIBLE);
                                    selected = 0;
                                    longSelected = true;
                                }

                            }
                        });
                    }
                    @NonNull
                    @Override
                    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        context = parent.getContext();
                        View view;

                        LayoutInflater inflater = LayoutInflater.from(context);
                        view = inflater.inflate(R.layout.notes_card, parent, false);

                        return new NoteViewHolder(view);
                    }
                    @Override
                    public int getItemCount() {
                        return notes.size();
                    }
                };

                categoryViewHolder.category_recyclerView.setAdapter(adapterNotes);
            }
            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                context = parent.getContext();
                View v1 = LayoutInflater.from(context)
                        .inflate(R.layout.category_card,parent,false);

                return new CategoryViewHolder(v1);
            }
            @Override
            public int getItemCount() {
                Log.d("NoteSize", String.valueOf(categoryNotes.size()));
                return categoryNotes.size();
            }
        };
        recyclerView.setAdapter(adapter);
    }


    private void deleteNote(final String noteId){

        Log.d(TAG, "deleteNote: DeleteButtonPressed" + noteId);

        refNote.child(noteId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Note Removed: " + noteId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Note not Removed: " + e);
            }
        });

        for (Media m : medias){
            if(m.getNoteId().equals(noteId)){
                refMedia.child(m.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "deleteNote - Remove Media" + noteId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "deleteNote: onFailure");
                    }
                });
            }
        }

        for (Location l : locations){
            if(l.getNoteId().equals(noteId)){
                refLocation.child(l.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "deleteNote - Remove Media" + noteId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "deleteNote: onFailure");
                    }
                });
            }
        }

        for (CategoryNotes c : categoryNotes){
            Log.d(TAG, "deleteNote: Name: " + c.getCategoryName());
            Log.d(TAG, "deleteNote: Notes: " + c.getNotes().size());
        }
    }

    private void addNewNote(){
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("MODE", CreateNoteActivity.NOTE_MODE_CREATE);
        startActivity(intent);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //getNotesFromFirebase();
        Log.d(TAG, "onPostResume: ");
    }

    private void getNotesFromFirebase(String type){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setActivated(true);
        //Get data
        ValueEventListener noteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                notes.clear();
                allNotes.clear();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);

                    if(note.getUser().equals(user)) {
                        notes.add(note);
                        datesDescending.add(note.getNoteDateToDate());
                        Log.d(TAG, "note: " + noteSnapshot.getKey() + " Detail: " + note.toString());

                        Note allNote = new Note();
                        allNote.setId(note.getId());
                        allNote.setTitle(note.getTitle());
                        allNote.setCategory(note.getCategory());
                        allNote.setNote(note.getNote());
                        allNote.setNoteDate(note.getNoteDate());
                        allNote.setUser(note.getUser());
                        allNotes.add(allNote);
                    }
                }

                startCategoryNotes();
                getMediaFromFirebase();
                adapter.notifyDataSetChanged();
                //Log.d(TAG, "onDataChange: " + dataSnapshot.getKey() + " Note: " + note.getTitle());
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "onCancelled", databaseError.toException());
                // ...
            }
        };
        refNote.orderByChild(type).addValueEventListener(noteListener);
    }

    private void getMediaFromFirebase(){
        Query mediaQuery = refMedia;
        //Log.d(TAG, "note id: " + note.getId());
        mediaQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medias.clear();
                for (DataSnapshot mediaSnapshot : snapshot.getChildren()) {
                    Media media = mediaSnapshot.getValue(Media.class);

                    if (media != null) {
                        medias.add(media);
                        Log.d(TAG, "onDataChangeMedia: " + media.getId());
                    }
                }
                adapter.notifyDataSetChanged();

                if (adapterNotes != null) {
                    adapterNotes.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setActivated(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });
    }

    private void getLocationsFromFirebase(){
        Query mediaQuery = refLocation;
        //Log.d(TAG, "note id: " + note.getId());
        mediaQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locations.clear();
                for (DataSnapshot mediaSnapshot : snapshot.getChildren()) {
                    Location location = mediaSnapshot.getValue(Location.class);

                    if (location != null) {
                        locations.add(location);
                        Log.d(TAG, "onDataChangeMedia: " + location.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });
    }

    private void startCategoryNotes(){
        startCategories();
        Log.d("Cat", categories.toString());
        categoryNotes.clear();
        for(int x=0;x<categories.size();x++){
            CategoryNotes cn = new CategoryNotes();
            for(int y=0; y<notes.size();y++){
                if(categories.get(x).equals(notes.get(y).getCategory())){
                    cn.getNotes().add(notes.get(y));
                }
            }
            cn.setCategoryName(categories.get(x));
            Log.d(TAG, "startCategoryNotes: " + categories.get(x));
            categoryNotes.add(cn);
        }
    }

    private void startCategories(){
        categories = new ArrayList<>();
        ArrayList<Note> noteDateDescending = new ArrayList<>();

        if(typeSorting.equals("id") || typeSorting.equals("noteDate")) {
            Collections.sort(datesDescending, Collections.<Date>reverseOrder());
        }

        for(Date d : datesDescending){
            for (Note not: notes){
                if(d.equals(not.getNoteDateToDate())){
                    noteDateDescending.add(not);
                    Log.d(TAG, "startCategories: " + not.getNoteDateToDate());
                }
                if(checkCategories(not.getCategory())) {
                    categories.add(not.getCategory());
                }
            }
        }

        Collections.sort(categories);

        Log.d("Cat", categories.toString());
        if(typeSorting.equals("noteDate") || typeSorting.equals("id")){
            notes = noteDateDescending;
        }

        datesDescending = new ArrayList<>();

    }

    private boolean checkCategories(String cat){
        if (categories.isEmpty()){return true;}
        for(int i=0;i<categories.size();i++) {
            if (categories.get(i).equals(cat)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notes_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.notes_sortAlphabetical:
                Log.d(TAG, "onOptionsItemSelected: Alphabetical" + refNote.getParent().orderByChild("title"));
                typeSorting = "title";
                getNotesFromFirebase("title");
                return true;
            case R.id.notes_sortDate:
                Log.d(TAG, "onOptionsItemSelected: Date");
                typeSorting = "noteDate";
                getNotesFromFirebase("noteDate");
                return true;
            case R.id.notes_sign_out:
                mAuth.signOut();
                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                 GoogleSignIn.getClient(this, googleSignInOptions).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         Log.d(TAG, "onOptionsItemSelected: Sign Out");
                         Toast.makeText(getApplicationContext(), "Sign out", Toast.LENGTH_SHORT).show();
                         finish();
                     }
                 });

                return true;
            default:
        }
        return false;
    }

    //

}