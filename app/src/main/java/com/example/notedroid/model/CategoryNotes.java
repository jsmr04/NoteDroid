package com.example.notedroid.model;

import java.util.ArrayList;

public class CategoryNotes {
    private String categoryName;
    private ArrayList<Note> notes = new ArrayList<Note>();
    private ArrayList<Media> medias = new ArrayList<Media>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    public ArrayList<Media> getMedias() {
        return medias;
    }

    public void setMedias(ArrayList<Media> medias) {
        this.medias = medias;
    }
}
