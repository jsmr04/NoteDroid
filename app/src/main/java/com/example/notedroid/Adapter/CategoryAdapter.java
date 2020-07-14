package com.example.notedroid.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notedroid.R;
import com.example.notedroid.model.CategoryNotes;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>  {

    private ArrayList<CategoryNotes> cn ;
    private RecyclerView.Adapter adapterNotes;
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView categoryName;
        public RecyclerView category_recyclerView;
        public RecyclerView.LayoutManager manager;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            manager = new LinearLayoutManager(itemView.getContext(),LinearLayoutManager.HORIZONTAL,false);
            categoryName = itemView.findViewById(R.id.category_name);
            category_recyclerView = itemView.findViewById(R.id.recyclerView);
            category_recyclerView.setLayoutManager(manager);

        }
    }
    public CategoryAdapter(ArrayList<CategoryNotes> categoryNotes){
        this.cn = categoryNotes;
    }


    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        holder.categoryName.setText(cn.get(position).getCategoryName());
        Log.d("Position", cn.get(position).getCategoryName());
        Log.d("Position", String.valueOf(position));
        adapterNotes = new NoteAdapter(cn.get(position).getNotes());
        adapterNotes.notifyDataSetChanged();
        holder.category_recyclerView.setAdapter(adapterNotes);
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v1 = LayoutInflater.from(context)
                .inflate(R.layout.category_card,parent,false);
        CategoryAdapter.ViewHolder viewHolder = new CategoryAdapter.ViewHolder(v1);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        Log.d("NoteSize", String.valueOf(cn.size()));
        return cn.size();
    }
}