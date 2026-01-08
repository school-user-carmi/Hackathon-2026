package com.example.hackathon_2026;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvBaskets;
    private TextView tvWelcome;
    private BasketRepository basketRepository;
    private List<Basket> baskets = new ArrayList<>();
    private BasketAdapter adapter;
    private static final String PREFS_NAME = "SmartCartPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        basketRepository = new BasketRepository(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        rvBaskets = findViewById(R.id.rvBaskets);
        ImageView ivCamera = findViewById(R.id.ivCamera);
        Button btnAddBasket = findViewById(R.id.btnAddBasket);

        // Set Welcome Message
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "User");
        tvWelcome.setText("Hello, " + username);

        // Setup Recycler
        rvBaskets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BasketAdapter();
        rvBaskets.setAdapter(adapter);

        // Setup ItemTouchHelper for Swipe Actions
        setupSwipeActions();

        // Handlers
        btnAddBasket.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateBasketActivity.class));
        });

        ivCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Scanner coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Basket basket = baskets.get(position);

                // Show Options Dialog
                showEditDeleteOptions(basket, position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                 // Optional: Draw background or icon here to indicate options
                 // For now, default behavior is fine, the dialog is the main requirement.
                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvBaskets);
    }

    private void showEditDeleteOptions(Basket basket, int position) {
        String[] options = {"Edit", "Delete", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Manage " + basket.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Edit
                        Intent intent = new Intent(MainActivity.this, CreateBasketActivity.class);
                        intent.putExtra(CreateBasketActivity.EXTRA_BASKET_ID, basket.id);
                        startActivity(intent);
                        adapter.notifyItemChanged(position); // Reset swipe state
                    } else if (which == 1) { // Delete
                        basketRepository.deleteBasket(basket.id);
                        loadBaskets();
                        Toast.makeText(MainActivity.this, "Basket deleted", Toast.LENGTH_SHORT).show();
                    } else { // Cancel
                        adapter.notifyItemChanged(position); // Reset swipe state
                    }
                })
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position)) // Reset on outside click
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBaskets();
    }

    private void loadBaskets() {
        baskets = basketRepository.getAllBaskets();
        adapter.notifyDataSetChanged();
    }

    private void showBasketResult(Basket basket) {
        PriceEngine.BasketResult result = PriceEngine.calculateBasketBestDeal(basket.products);

        if (result == null) {
            Toast.makeText(this, "Error calculating prices", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Cheapest Store: ").append(result.storeName).append("\n");
        message.append(String.format(Locale.getDefault(), "Total Price: %.2f ₪\n\n", result.totalPrice));

        if (!result.missingItems.isEmpty()) {
            message.append("Missing Items in this store:\n");
            for (String item : result.missingItems) {
                message.append("- ").append(item).append("\n");
            }
        } else {
            message.append("All items available!");
        }

        new AlertDialog.Builder(this)
                .setTitle("Best Deal for " + basket.name)
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_basket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Basket basket = baskets.get(position);
            holder.tvName.setText(basket.name);
            int count = (basket.products != null) ? basket.products.size() : 0;
            holder.tvInfo.setText(count + " items");

            holder.itemView.setOnClickListener(v -> showBasketResult(basket));

            // Kept Long click just in case, but swipe is the main way now.
             holder.itemView.setOnLongClickListener(v -> {
                 new AlertDialog.Builder(MainActivity.this)
                         .setTitle("Delete Basket")
                         .setMessage("Delete " + basket.name + "?")
                         .setPositiveButton("Yes", (d, w) -> {
                             basketRepository.deleteBasket(basket.id);
                             loadBaskets();
                         })
                         .setNegativeButton("No", null)
                         .show();
                 return true;
             });
        }

        @Override
        public int getItemCount() {
            return baskets.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvInfo;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvBasketName);
                tvInfo = itemView.findViewById(R.id.tvBasketInfo);
            }
        }
    }
}
