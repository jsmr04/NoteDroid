package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "NoteDroid";
    private FirebaseAuth mAuth;
    private TextView emailTextView;
    private TextView passwordTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailTextView = findViewById(R.id.login_email);
        passwordTextView = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.signin_progressBar);

        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser != null) {
//            Log.d(TAG, "User: " + currentUser.getEmail());
//        }

    }

    public void startProgress(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void stopProgress(){
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void signIn(View view){
        String email = emailTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        startProgress();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            stopProgress();
                            goToNotes();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showMessage(getApplicationContext(),
                                    "Sign In",
                                    task.getException().getMessage(),
                                    "OK",
                                    null);
                            stopProgress();
                        }
                    }
                });
    }

    private void goToNotes(){
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }

    public void showMessage(Context context, String title, String message, String positiveText, DialogInterface.OnClickListener onClickListener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveText, onClickListener)
                .create()
                .show();
    }

}