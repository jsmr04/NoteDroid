package com.example.notedroid.model;

import java.util.ArrayList;

public class CategoryNotes {
    private String categoryName;
    private ArrayList<Note> notes = new ArrayList<Note>();
    private ArrayList<Note> allNotes = new ArrayList<Note>();

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

    public ArrayList<Note> getAllNotes() {
        return allNotes;
    }

    public void setAllNotes(ArrayList<Note> allNotes) {
        this.allNotes = allNotes;
    }
}
