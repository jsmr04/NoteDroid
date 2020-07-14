package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

//import com.example.notedroid.Adapter.CategoryAdapter;
import com.example.notedroid.Adapter.CategoryViewHolder;
import com.example.notedroid.Adapter.NoteViewHolder;
import com.example.notedroid.Interface.NoteOnClickInterface;
import com.example.notedroid.model.CategoryNotes;
import com.example.notedroid.model.Media;
import com.example.notedroid.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotesActivity extends AppCompatActivity{
    private static final String TAG = "NoteDroid";
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter, adapterNotes;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Note> notes = new ArrayList<Note>();
    private DatabaseReference refNote, refMedia;
    private FirebaseDatabase database;
    private ArrayList<Media> medias = new ArrayList<>();
    private ArrayList<Bitmap> images = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<String>();
    private ArrayList<CategoryNotes> categoryNotes = new ArrayList<CategoryNotes>();
    private String user = "";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        fab = findViewById(R.id.addNoteFAB);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.notes_recyclerview);
        progressBar = findViewById(R.id.notes_progressBar);

        user = Util.getPreference(this, "user");

        database = FirebaseDatabase.getInstance();
        refNote = database.getReference("note");
        refMedia = database.getReference("media");

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        //Log.d("Cat", this.notes.get(0).toString());

        setupRecyclerView();

        getNotesFromFirebase();

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
                                if (!isLongPressed){
                                    Intent intent = new Intent(NotesActivity.this, CreateNoteActivity.class);
                                    intent.putExtra("NOTE", notes.get(positionN));
                                    intent.putExtra("MODE", CreateNoteActivity.NOTE_MODE_EDIT);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onLongClick(View view, boolean isLongPressed) {
                                Log.d(TAG, "onLongClick: NotesActivity :" + isLongPressed);
                                if(isLongPressed){

                                    noteViewHolder.titleTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                                }else{
                                    noteViewHolder.titleTextView.setTextColor(Color.WHITE);
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
                        NoteViewHolder viewHolder = new NoteViewHolder(view);

                        return viewHolder;
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
                CategoryViewHolder viewHolder = new CategoryViewHolder(v1);

                return viewHolder;
            }
            @Override
            public int getItemCount() {
                Log.d("NoteSize", String.valueOf(categoryNotes.size()));
                return categoryNotes.size();
            }
        };
        recyclerView.setAdapter(adapter);
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

    private void getNotesFromFirebase(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setActivated(true);
        //Get data
        ValueEventListener noteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                notes.clear();
                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);

                    if(note.getUser().equals(user)) {
                        notes.add(note);
                        Log.d(TAG, "note: " + noteSnapshot.getKey() + " Detail: " + note.toString());
                    }
                }

                startCategoryNotes();

                for (CategoryNotes c : categoryNotes){
                    Log.d(TAG, "onCreate: " + c.getCategoryName());

                }

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
        refNote.addValueEventListener(noteListener);
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

                    if (media != null && media.getType().equals("IMAGE")) {
                        medias.add(media);
                        Log.d(TAG, "onDataChangeMedia: " + media.getId());
                    }
                }
                adapter.notifyDataSetChanged();
                adapterNotes.notifyDataSetChanged();

                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setActivated(false);
//                for (Media m : medias){
//                    Log.d(TAG, "onCreate: " + m.getMedia());
//                }

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
        for (int x=0; x<notes.size();x++){
            if (checkCategories(notes.get(x).getCategory())){
                categories.add(notes.get(x).getCategory());
            }
        }
        Collections.sort(categories);
        Log.d("Cat", categories.toString());
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

    //

}