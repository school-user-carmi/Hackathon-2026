package com.example.hackathon_2026;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO CHECK IF NEEDED : add double duplicate check + double field Layout
public class RegisterUsersActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText emailInput;
    private EditText passwordInput;
    private Button registerBtn, loginScreenBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private HashMap<String, Object> userValues;

    private EditText username;
    private String usernameClean = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_users);

        userValues = new HashMap<String, Object>();


        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);

        registerBtn = findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);

        loginScreenBtn = findViewById(R.id.login_screen_btn);
        loginScreenBtn.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    //TODO: COMPLETE isValidEmail
    public boolean isValidEmail(String mail) {
        return !mail.isEmpty();//CHANGE TO A REAL STATEMENT
    }

    //TODO: COMPLETE isValidPassword
    public boolean isValidPassword(String pass) {
        return !pass.isEmpty(); //CHANGE TO A REAL STATEMENT
    }

    public void saveUserOnDB() {
        userValues.put("username", usernameClean);
        db.collection("user-baskets").document(auth.getCurrentUser().getUid()).set(userValues);
    }

    public boolean showUsernamePopup() {
        View dialogView = getLayoutInflater().inflate(R.layout.username_popup, null);
        username = dialogView.findViewById(R.id.username);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(this, "❌ Username not saved, account deleted", Toast.LENGTH_SHORT).show();
            auth.getCurrentUser().delete();//TODO: NULL CHECK

        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            okButton.setOnClickListener(v -> {
                usernameClean = username.getText().toString().trim();

                if (!usernameClean.isEmpty()) {//TODO? ADD USERNAME CHECK METHOD
                    Log.d("art","save user  works");
                    saveUserOnDB();
                    dialog.dismiss();
                    Toast.makeText(this, "Username saved, account created", Toast.LENGTH_SHORT).show();


                } else {
                    username.setError("❌ Please enter a valid username");
                }

            });

        });
        dialog.show();
        return !usernameClean.isEmpty();
    }


    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
        if (v.getId() == registerBtn.getId()) {
            String emailClean = emailInput.getText().toString().trim();
            String passwordClean = passwordInput.getText().toString().trim();
            if (isValidEmail(emailClean) && isValidPassword(passwordClean)) {
                auth.createUserWithEmailAndPassword(emailClean, passwordClean).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (showUsernamePopup()) {
                            Toast.makeText(this, "✅ Registration successful!", Toast.LENGTH_SHORT).show();
                            //TODO: SEND TO GAME MENU
                        }
                    } else {
                        Toast.makeText(this, "❌ Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();//TODO FIX NULL EXCEPTION
                    }
                });
            } else {
                Toast.makeText(this, "❌ Error: Invalid email or password", Toast.LENGTH_LONG).show();
            }
        }
        if (v.getId() == loginScreenBtn.getId()) {
            Intent intent = new Intent(this, LoginUsersActivity.class);
            intent.putExtra("email", emailInput.getText().toString().trim());//TODO: check empty implication
            startActivity(intent);
        }
    }
}

