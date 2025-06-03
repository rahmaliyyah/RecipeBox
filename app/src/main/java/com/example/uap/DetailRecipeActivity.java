package com.example.uap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailRecipeActivity extends AppCompatActivity {
    private static final String TAG = "DetailRecipeActivity";

    private ImageButton btnBack;
    private ImageView ivRecipeDetailImage;
    private TextView tvRecipeTitle, tvCookingTime, tvIngredients, tvInstructions;
    private MaterialButton btnEditRecipe, btnDeleteRecipe;

    private DatabaseReference databaseReference;
    private String recipeId;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_recipe);

        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        recipeId = getIntent().getStringExtra("RECIPE_ID");

        if (recipeId == null) {
            Toast.makeText(this, "ID resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadRecipeDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipeDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_detail);
        ivRecipeDetailImage = findViewById(R.id.iv_recipe_detail_image);
        tvRecipeTitle = findViewById(R.id.tv_recipe_title);
        tvCookingTime = findViewById(R.id.tv_cooking_time);
        tvIngredients = findViewById(R.id.tv_ingredients);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnEditRecipe = findViewById(R.id.btn_edit_recipe_detail);
        btnDeleteRecipe = findViewById(R.id.btn_delete_recipe_detail);

        Log.d(TAG, "Views initialized");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(DetailRecipeActivity.this, EditRecipeActivity.class);
            intent.putExtra("RECIPE_ID", recipeId);
            startActivity(intent);
        });

        btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadRecipeDetails() {
        Log.d(TAG, "Loading recipe details for ID: " + recipeId);

        databaseReference.child(recipeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Recipe data received from Firebase");

                currentRecipe = dataSnapshot.getValue(Recipe.class);
                if (currentRecipe != null) {
                    currentRecipe.setId(dataSnapshot.getKey());
                    Log.d(TAG, "Recipe loaded: " + currentRecipe.getName());
                    Log.d(TAG, "Image URL: " + currentRecipe.getImageUrl());
                    displayRecipeDetails();
                } else {
                    Log.e(TAG, "Recipe not found in database");
                    Toast.makeText(DetailRecipeActivity.this,
                            "Resep tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(DetailRecipeActivity.this,
                        "Gagal memuat detail resep: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipeDetails() {
        Log.d(TAG, "Displaying recipe details");

        tvRecipeTitle.setText(currentRecipe.getName());
        tvCookingTime.setText(currentRecipe.getCookingTime() + " menit");
        tvIngredients.setText(currentRecipe.getIngredients());
        tvInstructions.setText(currentRecipe.getInstructions());

        loadRecipeImage();
    }

    private void loadRecipeImage() {
        Log.d(TAG, "=== LOADING RECIPE IMAGE ===");

        if (currentRecipe.getImageUrl() != null && !currentRecipe.getImageUrl().isEmpty()) {
            Log.d(TAG, "Image URL found, length: " + currentRecipe.getImageUrl().length());

            try {
                // Use the same method as RecipeAdapter for consistency
                loadImageFromBase64(currentRecipe.getImageUrl(), ivRecipeDetailImage);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage(), e);
                ivRecipeDetailImage.setImageResource(R.drawable.placeholder_food);
            }
        } else {
            Log.d(TAG, "No image URL found, showing placeholder");
            ivRecipeDetailImage.setImageResource(R.drawable.placeholder_food);
        }
    }

    // Use the exact same method as RecipeAdapter for image loading consistency
    private void loadImageFromBase64(String base64String, ImageView imageView) {
        try {
            if (base64String != null && !base64String.isEmpty()) {
                Log.d(TAG, "Loading image from Base64, length: " + base64String.length());

                // Remove data URL prefix if present
                String base64Data = base64String;
                if (base64String.startsWith("data:image")) {
                    base64Data = base64String.substring(base64String.indexOf(",") + 1);
                    Log.d(TAG, "Removed data URL prefix");
                }

                // Clean up the base64 string - same as RecipeAdapter
                base64Data = base64Data.replaceAll("\\s", "").trim();

                byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    Log.d(TAG, "✓ Image loaded successfully from Base64");
                } else {
                    imageView.setImageResource(R.drawable.placeholder_food);
                    Log.w(TAG, "Failed to decode bitmap from Base64, using placeholder");
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder_food);
                Log.d(TAG, "No image data, using placeholder");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from Base64: " + e.getMessage(), e);
            imageView.setImageResource(R.drawable.placeholder_food);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Resep")
                .setMessage("Apakah Anda yakin ingin menghapus resep ini?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteRecipe())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteRecipe() {
        databaseReference.child(recipeId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DetailRecipeActivity.this,
                                "Resep berhasil dihapus!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(DetailRecipeActivity.this,
                                "Gagal menghapus resep: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}