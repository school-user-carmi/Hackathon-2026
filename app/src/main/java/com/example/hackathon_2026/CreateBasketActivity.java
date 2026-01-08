package com.example.hackathon_2026;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateBasketActivity extends AppCompatActivity {

    public static final String EXTRA_BASKET_ID = "basket_id";

    private List<Product> allProducts;
    private List<Product> filteredProducts = new ArrayList<>();
    private List<Product> basketProducts = new ArrayList<>();

    private ProductAdapter searchAdapter;
    private TextView tvBasketCount;
    private TextView tvTitle;

    private String editingBasketId = null;
    private String editingBasketName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_basket);

        // Load all products
        allProducts = DataLoader.loadProducts(this);
        if (allProducts == null) allProducts = new ArrayList<>();
        filteredProducts.addAll(allProducts);

        tvTitle = findViewById(R.id.tvTitle);
        EditText etSearch = findViewById(R.id.etSearchProduct);
        RecyclerView rvProducts = findViewById(R.id.rvProducts);

        tvBasketCount = findViewById(R.id.tvBasketCount);
        Button btnViewBasket = findViewById(R.id.btnViewBasket);
        Button btnSave = findViewById(R.id.btnSaveBasket);

        // Setup Search Adapter
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new ProductAdapter();
        rvProducts.setAdapter(searchAdapter);

        // Check Intent for Edit Mode
        if (getIntent().hasExtra(EXTRA_BASKET_ID)) {
            editingBasketId = getIntent().getStringExtra(EXTRA_BASKET_ID);
            loadBasketForEditing(editingBasketId);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> showSaveDialog());
        btnViewBasket.setOnClickListener(v -> showBasketItemsDialog());

        updateBasketCount();
    }

    private void loadBasketForEditing(String basketId) {
        BasketRepository repo = new BasketRepository(this);
        Basket basket = repo.getBasket(basketId);
        if (basket != null) {
            editingBasketName = basket.name;
            if (basket.products != null) {
                basketProducts.addAll(basket.products);
            }
            tvTitle.setText("Edit Basket: " + editingBasketName);
            updateBasketCount();
        } else {
            Toast.makeText(this, "Error loading basket", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateBasketCount() {
        tvBasketCount.setText("Items: " + basketProducts.size());
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.name.toLowerCase().contains(query.toLowerCase())) {
                    filteredProducts.add(p);
                }
            }
        }
        searchAdapter.notifyDataSetChanged();
    }

    private void showBasketItemsDialog() {
        if (basketProducts.isEmpty()) {
            Toast.makeText(this, "Your basket is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_basket_items, null);
        RecyclerView rvDialogItems = dialogView.findViewById(R.id.rvDialogBasketItems);
        Button btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        // Setup Adapter for the dialog
        rvDialogItems.setLayoutManager(new LinearLayoutManager(this));
        BasketItemAdapter adapter = new BasketItemAdapter();
        rvDialogItems.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSaveDialog() {
        if (basketProducts.isEmpty()) {
            Toast.makeText(this, "Basket is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(editingBasketId != null ? "Update Basket" : "Name your basket");

        final EditText input = new EditText(this);
        input.setHint("Basket Name");
        if (editingBasketName != null) {
            input.setText(editingBasketName);
        }
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) name = "Basket " + System.currentTimeMillis();

            saveBasket(name);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveBasket(String name) {
        String id = (editingBasketId != null) ? editingBasketId : UUID.randomUUID().toString();

        Basket basket = new Basket(id, name, basketProducts);
        BasketRepository repo = new BasketRepository(this);
        repo.saveBasket(basket);

        Toast.makeText(this, "Basket saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // --- Helper Class for Grouping ---
    private static class GroupedItem {
        Product product;
        int count;

        GroupedItem(Product product, int count) {
            this.product = product;
            this.count = count;
        }
    }

    // --- Search Result Adapter ---
    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_search, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = filteredProducts.get(position);
            holder.tvName.setText(product.name);
            holder.btnAdd.setOnClickListener(v -> {
                basketProducts.add(product);
                updateBasketCount();
                Toast.makeText(CreateBasketActivity.this, "Added " + product.name, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return filteredProducts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            Button btnAdd;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProductName);
                btnAdd = itemView.findViewById(R.id.btnAddProduct);
            }
        }
    }

    // --- Basket Item Adapter (Used in Dialog with Grouping) ---
    private class BasketItemAdapter extends RecyclerView.Adapter<BasketItemAdapter.ViewHolder> {

        private List<GroupedItem> groupedItems;

        BasketItemAdapter() {
            refreshGroupedItems();
        }

        private void refreshGroupedItems() {
            Map<String, GroupedItem> map = new HashMap<>();
            // Assuming Product has a unique 'barcode' or 'name'. Using name as key for now since barcode isn't always reliable in mocks.
            // If strict unique ID exists, prefer that.

            for (Product p : basketProducts) {
                // Ideally use ID/Barcode. Assuming name is unique enough for this hackathon context.
                String key = (p.barcode != null && !p.barcode.isEmpty()) ? p.barcode : p.name;

                if (map.containsKey(key)) {
                    map.get(key).count++;
                } else {
                    map.put(key, new GroupedItem(p, 1));
                }
            }
            groupedItems = new ArrayList<>(map.values());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_search, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupedItem item = groupedItems.get(position);

            // Display: "Product Name (3x)" or just "Product Name"
            String displayText = item.product.name;
            if (item.count > 1) {
                displayText += " (" + item.count + "x)";
            }
            holder.tvName.setText(displayText);

            // Change button to "Remove"
            holder.btnAdd.setText("Remove");
            holder.btnAdd.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            holder.btnAdd.setOnClickListener(v -> {
                removeOneInstance(item.product);
                refreshGroupedItems();
                notifyDataSetChanged(); // Since grouping changes the whole structure, easier to reload all
                updateBasketCount();

                if (basketProducts.isEmpty()) {
                     Toast.makeText(CreateBasketActivity.this, "Basket is now empty", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void removeOneInstance(Product productToRemove) {
             // We need to find *one* instance in the main list and remove it.
             // We match by barcode or name again.
             for (int i = 0; i < basketProducts.size(); i++) {
                 Product p = basketProducts.get(i);
                 String keyP = (p.barcode != null && !p.barcode.isEmpty()) ? p.barcode : p.name;
                 String keyTarget = (productToRemove.barcode != null && !productToRemove.barcode.isEmpty()) ? productToRemove.barcode : productToRemove.name;

                 if (keyP.equals(keyTarget)) {
                     basketProducts.remove(i);
                     return; // Remove only one
                 }
             }
        }

        @Override
        public int getItemCount() {
            return groupedItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            Button btnAdd;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProductName);
                btnAdd = itemView.findViewById(R.id.btnAddProduct);
            }
        }
    }
}
