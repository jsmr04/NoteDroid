package com.example.notedroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notedroid.model.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "NoteDroid";
    private FirebaseAuth mAuth;

    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;
    private TextView confirmPasswordTextView;
    private ProgressBar progressBar;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstNameTextView = findViewById(R.id.signup_first_name);
        lastNameTextView = findViewById(R.id.signup_last_name);
        emailTextView = findViewById(R.id.signup_emailAddress);
        passwordTextView = findViewById(R.id.signup_password);
        confirmPasswordTextView = findViewById(R.id.signup_confirm_password);
        layout = findViewById(R.id.signup_layout);
        progressBar = findViewById(R.id.signup_progressBar);

        mAuth = FirebaseAuth.getInstance();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
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

    public void createUser(View view){

        Log.d(TAG, "createUser: Click");
        if (checkFields()) {
            String email = emailTextView.getText().toString();
            String password = passwordTextView.getText().toString();
            String firstName = firstNameTextView.getText().toString();
            String lastName = lastNameTextView.getText().toString();
            final String  displayName = firstName + " " + lastName;

            startProgress();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(displayName)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG, "update profile:success");
                                            //Util.showMessage(getApplicationContext(),"Sign Up","User created successfully","OK",null);
                                            showMessage(getApplicationContext(),
                                                    "Sign Up",
                                                    "User created successfully",
                                                    "OK",
                                                    null);

                                        }else{
                                            Log.w(TAG, "update profile:failure", task.getException());
                                            showMessage(getApplicationContext(),
                                                    "Sign Up",
                                                    task.getException().getMessage(),
                                                    "OK",
                                                    null);
                                        }

                                        stopProgress();
                                    }
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                                showMessage(getApplicationContext(),
                                        "Sign Up",
                                        task.getException().getMessage(),
                                        "OK",
                                        null);
                                stopProgress();
                                //updateUI(null);
                            }
                        }
                    });
        }
    }

    private boolean checkFields(){

        if (firstNameTextView.getText().toString().trim().equals("")){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "First name is required.",
                    "OK",
                    null);
            firstNameTextView.requestFocus();
            return false;
        }

        if (lastNameTextView.getText().toString().trim().equals("")){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "Last name is required.",
                    "OK",
                    null);
            lastNameTextView.requestFocus();
            return false;
        }

        if (emailTextView.getText().toString().trim().equals("")){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "Email is required.",
                    "OK",
                    null);
            emailTextView.requestFocus();
            return false;
        }

        if (!emailTextView.getText().toString().contains("@") || !emailTextView.getText().toString().contains(".")){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "Email is not valid.",
                    "OK",
                    null);
            emailTextView.requestFocus();
            return false;
        }

        if (passwordTextView.getText().toString().trim().equals("")){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "Password is required.",
                    "OK",
                    null);
            passwordTextView.requestFocus();
            return false;
        }

        if (!passwordTextView.getText().toString().trim().equals(confirmPasswordTextView.getText().toString().trim())){
            showMessage(getApplicationContext(),
                    "Sign Up",
                    "Passwords must match.",
                    "OK",
                    null);
            confirmPasswordTextView.requestFocus();
            return false;
        }

        return true;
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