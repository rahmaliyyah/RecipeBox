package com.example.uap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AllRecipesActivity extends AppCompatActivity {
    private static final String TAG = "AllRecipesActivity";

    private RecyclerView rvRecipes;
    private ImageView btnBack, btnAddRecipe;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ValueEventListener recipeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_recipe_activity);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        Log.d(TAG, "onResume called - reloading recipes");
        loadRecipes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (recipeListener != null && databaseReference != null) {
            databaseReference.removeEventListener(recipeListener);
            Log.d(TAG, "Removed Firebase listener");
        }
    }

    private void initViews() {
        rvRecipes = findViewById(R.id.rv_recipes);
        btnBack = findViewById(R.id.btn_back);
        btnAddRecipe = findViewById(R.id.btn_add_recipe);

        Log.d(TAG, "Views initialized");
    }

    private void setupRecyclerView() {
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipeList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRecipes.setLayoutManager(layoutManager);
        rvRecipes.setAdapter(recipeAdapter);

        // Add item decoration for better spacing
        rvRecipes.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));

        Log.d(TAG, "RecyclerView setup complete");
        Log.d(TAG, "Initial adapter item count: " + recipeAdapter.getItemCount());
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked - signing out");
            firebaseAuth.signOut();
            Intent intent = new Intent(AllRecipesActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnAddRecipe.setOnClickListener(v -> {
            Log.d(TAG, "Add recipe button clicked");
            Intent intent = new Intent(AllRecipesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void loadRecipes() {
        if (firebaseAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        Log.d(TAG, "Loading recipes for user: " + currentUserId);
        if (recipeListener != null) {
            databaseReference.removeEventListener(recipeListener);
        }

        recipeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== FIREBASE DATA RECEIVED ===");
                Log.d(TAG, "Total recipes in database: " + dataSnapshot.getChildrenCount());
                Log.d(TAG, "Current user ID: " + currentUserId);

                // Create new list for this update
                List<Recipe> newRecipeList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Recipe recipe = snapshot.getValue(Recipe.class);

                        if (recipe != null) {
                            recipe.setId(snapshot.getKey());

                            Log.d(TAG, "=== RECIPE FOUND ===");
                            Log.d(TAG, "Recipe ID: " + snapshot.getKey());
                            Log.d(TAG, "Recipe Name: " + recipe.getName());
                            Log.d(TAG, "Recipe UserID: " + recipe.getUserId());
                            Log.d(TAG, "Recipe CookingTime: " + recipe.getCookingTime());
                            Log.d(TAG, "User ID Match: " + (recipe.getUserId() != null && recipe.getUserId().equals(currentUserId)));

                            // Filter by current user
                            if (recipe.getUserId() != null && recipe.getUserId().equals(currentUserId)) {
                                newRecipeList.add(recipe);
                                Log.d(TAG, "✓ Recipe added to list: " + recipe.getName());
                            } else {
                                Log.d(TAG, "✗ Recipe skipped - UserID mismatch");
                            }
                        } else {
                            Log.e(TAG, "Recipe is null for snapshot: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing recipe: " + e.getMessage(), e);
                    }
                }

                Log.d(TAG, "=== FINAL RESULTS ===");
                Log.d(TAG, "Total recipes loaded for user: " + newRecipeList.size());

                // Update UI on main thread
                runOnUiThread(() -> {
                    updateRecipesList(newRecipeList);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage(), databaseError.toException());
                runOnUiThread(() -> {
                    Toast.makeText(AllRecipesActivity.this,
                            "Gagal memuat resep: " + databaseError.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        };

        // Attach listener
        databaseReference.addValueEventListener(recipeListener);
        Log.d(TAG, "Firebase listener attached");
    }

    private void updateRecipesList(List<Recipe> newRecipeList) {
        Log.d(TAG, "=== UPDATING RECIPES LIST ===");
        Log.d(TAG, "New recipes count: " + newRecipeList.size());

        // Clear and update the main list
        recipeList.clear();
        recipeList.addAll(newRecipeList);

        Log.d(TAG, "Main recipeList updated, size: " + recipeList.size());

        // Update adapter
        if (recipeAdapter != null) {
            recipeAdapter.updateRecipes(newRecipeList);

            // Force a refresh to make sure
            recipeAdapter.refreshAdapter();

            Log.d(TAG, "Adapter updated, final count: " + recipeAdapter.getItemCount());

            // Show appropriate message
            if (newRecipeList.isEmpty()) {
                Toast.makeText(this,
                        "Belum ada resep. Tambahkan resep pertama Anda!",
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "No recipes found for current user");
            } else {
                Toast.makeText(this,
                        "Berhasil memuat " + newRecipeList.size() + " resep",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Successfully loaded " + newRecipeList.size() + " recipes");

                // Log first recipe for verification
                Recipe firstRecipe = newRecipeList.get(0);
                Log.d(TAG, "First recipe: " + firstRecipe.getName() + " (ID: " + firstRecipe.getId() + ")");
            }
        } else {
            Log.e(TAG, "RecipeAdapter is null!");
        }

        postDelayed(() -> {
            Log.d(TAG, "=== POST-UPDATE VERIFICATION ===");
            Log.d(TAG, "RecyclerView child count: " + rvRecipes.getChildCount());
            Log.d(TAG, "Adapter item count: " + (recipeAdapter != null ? recipeAdapter.getItemCount() : "null"));
            Log.d(TAG, "Main list size: " + recipeList.size());

            if (recipeAdapter != null && recipeAdapter.getCurrentRecipes() != null) {
                Log.d(TAG, "Adapter internal list size: " + recipeAdapter.getCurrentRecipes().size());
            }
        }, 1000);
    }

    private void postDelayed(Runnable runnable, long delayMillis) {
        rvRecipes.postDelayed(runnable, delayMillis);
    }
}