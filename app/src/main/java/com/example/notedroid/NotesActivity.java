package com.example.notedroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.example.notedroid.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {
    private static final String TAG = "NoteDroid";
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Note> notes = new ArrayList<Note>();
    private DatabaseReference refNote;
    private FirebaseDatabase database;
    private String user = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        fab = findViewById(R.id.addNoteFAB);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.notes_recyclerview);

        user = Util.getPreference(this, "user");

        database = FirebaseDatabase.getInstance();
        refNote = database.getReference("note");

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NoteAdapter(notes);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                goToEdit(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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

    private void goToEdit(int index){
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("NOTE", notes.get(index));
        intent.putExtra("MODE", CreateNoteActivity.NOTE_MODE_EDIT);
        startActivity(intent);
    }

    private void getNotesFromFirebase(){
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

}