package com.example.hackathon_2026;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUsersActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginBtn, resetPassword;
    private ImageButton backBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private Exception e;
    private Intent intent_back;
    private Intent intent_camera_ocr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_users);

        emailInput = findViewById(R.id.email_login);
        passwordInput = findViewById(R.id.password_login);

        loginBtn = findViewById(R.id.login_btn);
        backBtn = findViewById(R.id.back_btn);
        resetPassword = findViewById(R.id.reset_password_btn);

        loginBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        resetPassword.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent in = getIntent();
        if (in.hasExtra("email")) {
            emailInput.setText(in.getStringExtra("email"));
        }

        intent_back = new Intent(this, RegisterUsersActivity.class);
        intent_camera_ocr = new Intent(this, cameraTester.class);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

    //TODO: COMPLETE isValidEmail
    public boolean isValidEmail(String mail) {
        return !mail.isEmpty();//CHANGE TO A REAL STATEMENT
    }

    //TODO: COMPLETE isValidPassword
    public boolean isValidPassword(String pass) {
        return !pass.isEmpty(); //CHANGE TO A REAL STATEMENT
    }

    public Toast errorMsg() {
        Exception e = this.e;
        if (e instanceof FirebaseAuthInvalidUserException) {
            return Toast.makeText(this, "No Account with that email please try again or move back to the register page to create an account"
                    , Toast.LENGTH_LONG);

        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return Toast.makeText(this, "Invalid password please try again", Toast.LENGTH_LONG);
        }
        return Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG);
    }

    public void resetPassword() {
        if (isValidEmail(emailInput.getText().toString())) {
            auth.sendPasswordResetEmail(emailInput.getText().toString()).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Toast.makeText(this, "✅ Password reset email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    this.e = task.getException();
                    errorMsg().show();
                }
            });
        }
    }

    public void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = "";
        if (isValidEmail(email) && isValidPassword(password)) {
            auth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString()).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    //TODO: SEND TO GAME MENU
                    user = auth.getCurrentUser();
                    if (user != null) {
                        Log.d("**!!!***", "worked!!!" + user.getUid());
                        Toast.makeText(this, "✅ Login successful!", Toast.LENGTH_SHORT).show();
                        Log.d("**!!!***", "reach after toast");
                        db.collection("user-baskets").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                            documentSnapshot.getData().forEach((key, value) -> {
                                switch (key) {
                                    case "username":
                                        Toast.makeText(this, "✅ Welcome " + value.toString() + "!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, MainActivity.class);
                                        startActivity(intent);
                                        break;
                                }
                            });
                        });
                    } else {

                        this.e = task.getException();
                        errorMsg().show();
                    }

                } else {
                    Toast.makeText(this, "❌ Error: Invalid email or password please try again", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == loginBtn.getId()) {
            login();
        } else if (v.getId() == resetPassword.getId()) {
            resetPassword();
        } else if (v.getId() == backBtn.getId()) {
            startActivity(intent_back);
        }
    }
}