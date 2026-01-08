package com.example.hackathon_2026;

import android.content.Context;
import android.os.Bundle;
import android.util.Log; // חשוב להוסיף את זה
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hackathon_2026.Utils.Utils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // משתנה שיחזיק את רשימת המוצרים לאורך כל חיי האפליקציה
    private List<Product> allProducts;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // הגדרות עיצוב המערכת (היו לך כבר)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- כאן מתחיל החיבור של הדאטה ---

        // טעינת הנתונים מה-JSON באמצעות המחלקה שיצרנו
        allProducts = DataLoader.loadProducts(this);

        // בדיקה ב-Logcat כדי לראות אם זה עובד
        if (allProducts != null) {
            Log.d("HACKATHON_DATA", "הצלחנו! נטענו " + allProducts.size() + " מוצרים.");
        } else {
            Log.e("HACKATHON_DATA", "שגיאה: הנתונים לא נטענו. בדוק את תיקיית res/raw ואת מחלקת ה-Product.");
        }


    }
}