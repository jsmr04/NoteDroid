package com.example.notedroid.Adapter;

import android.view.View;;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notedroid.Interface.NoteOnClickInterface;
import com.example.notedroid.R;


public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView noteImageView;
    public TextView titleTextView;
    public TextView dateTextView;
    public NoteOnClickInterface noteOnClickInterface;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        noteImageView = itemView.findViewById(R.id.note_ImageView);
        titleTextView = itemView.findViewById(R.id.noteTitle_TextView);
        dateTextView = itemView.findViewById(R.id.noteDate_TextView);

    }
    @Override
    public void onClick(View v) { noteOnClickInterface.onClick(v,false);}
    public void NoteInterfaceClick(NoteOnClickInterface noteOnClickInterface) { this.noteOnClickInterface = noteOnClickInterface; }
}