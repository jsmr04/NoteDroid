package com.example.notedroid.Interface;

import android.view.View;

public interface NoteOnClickInterface {
    void onClick(View view, boolean isLongPressed);
    void onLongClick(View view, boolean isLongPressed);
}
