package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "NoteDroid";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("note");
// Create Notes
//        String key = myRef.child("note").push().getKey();
//        Log.d(TAG, "KEY: " + key);
//
//        Note note = new Note();
//        note.setId(key);
//        note.setTitle("New Title3");
//        note.setNote("New Note");
//        note.setCategory("New Cat");
//        note.setLocation("New Location");
//        note.setNoteDate("New Date");
//        note.setUser("New User");
//
//        myRef.child(key).setValue(note).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "onSuccess");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "onFailure");
//            }
//        });

//Delete
//        myRef.child("MBaWj_oSIU5m4lLor68").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "onSuccess");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "onFailure");
//            }
//        });

//Get data
//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Get Post object and use the values to update the UI
//
//                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()) {
//                    Note note = noteSnapshot.getValue(Note.class);
//                    Log.d(TAG, "note: " + noteSnapshot.getKey() + " Detail: " + note.toString());
//                }
//
//                //Log.d(TAG, "onDataChange: " + dataSnapshot.getKey() + " Note: " + note.getTitle());
//                // ...
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "onCancelled", databaseError.toException());
//                // ...
//            }
//        };
//        myRef.addValueEventListener(postListener);

    }
}