package com.example.notedroid.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Note implements Serializable {

    private String id;
    private String title;
    private String note;
    private String category;
    private String noteDate;
    private String user;

    public Note(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public Date getNoteDateToDate() {
        Date newDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            newDate = format.parse(noteDate);
            System.out.println(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    public void setNoteDate(String noteDate) {
        this.noteDate = noteDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", category='" + category + '\'' +
                ", noteDate='" + noteDate + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
