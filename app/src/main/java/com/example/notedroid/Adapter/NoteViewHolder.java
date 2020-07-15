package com.example.notedroid.Adapter;

import android.util.Log;
import android.view.View;;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notedroid.Interface.NoteOnClickInterface;
import com.example.notedroid.R;

import static android.content.ContentValues.TAG;


public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public ImageView noteImageView;
    public ImageButton deleteIcon;
    public TextView titleTextView;
    public TextView dateTextView;
    public View noteView;
    public NoteOnClickInterface noteOnClickInterface;
    boolean isLongPress = false;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        noteImageView = itemView.findViewById(R.id.note_ImageView);
        titleTextView = itemView.findViewById(R.id.noteTitle_TextView);
        dateTextView = itemView.findViewById(R.id.noteDate_TextView);
        deleteIcon = itemView.findViewById(R.id.imageButtonDelete);
        noteView = itemView.findViewById(R.id.noteViewGradient);

    }
    @Override
    public void onClick(View v) { noteOnClickInterface.onClick(v, isLongPress);}

    public void NoteInterfaceClick(NoteOnClickInterface noteOnClickInterface) {
        this.noteOnClickInterface = noteOnClickInterface;
    }

    @Override
    public boolean onLongClick(View view) {
        if (isLongPress){
            noteOnClickInterface.onLongClick(view, false);
            isLongPress = false;
            Log.d(TAG, "onLongClick: false");
            return isLongPress;
        }else{
            noteOnClickInterface.onLongClick(view, true);
            isLongPress = true;
            Log.d(TAG, "onLongClick: true");
            return isLongPress;
        }

    }
}