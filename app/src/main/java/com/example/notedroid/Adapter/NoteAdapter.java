package com.example.notedroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notedroid.R;
import com.example.notedroid.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private ArrayList<Note> dataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView noteImageView;
        private TextView titleTextView;
        private TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(true);

            noteImageView = itemView.findViewById(R.id.note_ImageView);
            titleTextView = itemView.findViewById(R.id.noteTitle_TextView);
            dateTextView = itemView.findViewById(R.id.noteDate_TextView);

        }
    }

    public NoteAdapter(ArrayList<Note> dataset){
        this.dataset = dataset;
    }


    @NonNull
    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
       holder.noteImageView.setImageResource(R.drawable.welcome_background);
       holder.titleTextView.setText(dataset.get(position).getTitle());
       holder.dateTextView.setText(dataset.get(position).getNoteDate());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;

        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.notes_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;

    }
}