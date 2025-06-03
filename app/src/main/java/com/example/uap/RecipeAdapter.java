package com.example.uap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private static final String TAG = "RecipeAdapter";

    private Context context;
    private List<Recipe> recipeList;

    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = new ArrayList<>(recipeList != null ? recipeList : new ArrayList<>());
        Log.d(TAG, "RecipeAdapter created with " + this.recipeList.size() + " items");
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        Log.d(TAG, "ViewHolder created");
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        if (recipeList == null || position >= recipeList.size()) {
            Log.e(TAG, "Invalid position or null recipeList. Position: " + position +
                    ", Size: " + (recipeList != null ? recipeList.size() : "null"));
            return;
        }

        Recipe recipe = recipeList.get(position);
        Log.d(TAG, "=== BINDING RECIPE ===");
        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "Recipe: " + (recipe != null ? recipe.getName() : "null"));

        if (recipe == null) {
            Log.e(TAG, "Recipe is null at position " + position);
            return;
        }

        Log.d(TAG, "Cooking Time: " + recipe.getCookingTime());

        // Set recipe name
        if (recipe.getName() != null) {
            holder.tvRecipeName.setText(recipe.getName());
        } else {
            holder.tvRecipeName.setText("Unknown Recipe");
        }

        // Handle cooking time display
        if (recipe.getCookingTime() != null && !recipe.getCookingTime().isEmpty()) {
            String cookingTimeText = recipe.getCookingTime().trim();

            // Add "menit" if not already present
            if (!cookingTimeText.toLowerCase().contains("menit") &&
                    !cookingTimeText.toLowerCase().contains("jam") &&
                    !cookingTimeText.toLowerCase().contains("detik")) {
                cookingTimeText += " menit";
            }

            holder.tvCookingTime.setText(cookingTimeText);
            holder.tvCookingTime.setVisibility(View.VISIBLE);
            Log.d(TAG, "Cooking time set: " + cookingTimeText);
        } else {
            holder.tvCookingTime.setVisibility(View.GONE);
            Log.d(TAG, "Cooking time hidden (empty or null)");
        }

        // Load image from Base64 string
        loadImageFromBase64(recipe.getImageUrl(), holder.ivRecipeImage);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Recipe clicked: " + recipe.getName() + " (ID: " + recipe.getId() + ")");
            Intent intent = new Intent(context, DetailRecipeActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            context.startActivity(intent);
        });

        Log.d(TAG, "Recipe binding completed for: " + recipe.getName());
    }

    private void loadImageFromBase64(String base64String, ImageView imageView) {
        try {
            if (base64String != null && !base64String.isEmpty()) {
                Log.d(TAG, "Loading image from Base64, length: " + base64String.length());

                String base64Data = base64String;
                if (base64String.startsWith("data:image")) {
                    base64Data = base64String.substring(base64String.indexOf(",") + 1);
                    Log.d(TAG, "Removed data URL prefix");
                }

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

    @Override
    public int getItemCount() {
        int count = recipeList != null ? recipeList.size() : 0;
        Log.d(TAG, "getItemCount called, returning: " + count);
        return count;
    }

    public void updateRecipes(List<Recipe> newRecipeList) {
        Log.d(TAG, "=== UPDATING RECIPES ===");
        Log.d(TAG, "Previous size: " + (this.recipeList != null ? this.recipeList.size() : 0));
        Log.d(TAG, "New size: " + (newRecipeList != null ? newRecipeList.size() : 0));

        if (this.recipeList == null) {
            Log.e(TAG, "recipeList is null! Creating new ArrayList");
            this.recipeList = new ArrayList<>();
        }

        // Clear and add new data
        this.recipeList.clear();
        if (newRecipeList != null && !newRecipeList.isEmpty()) {
            this.recipeList.addAll(newRecipeList);

            // Log each recipe being added
            for (int i = 0; i < newRecipeList.size(); i++) {
                Recipe recipe = newRecipeList.get(i);
                if (recipe != null) {
                    Log.d(TAG, "Recipe " + i + ": " + recipe.getName() + " (ID: " + recipe.getId() + ")");
                } else {
                    Log.e(TAG, "Recipe " + i + " is null!");
                }
            }
        }

        // Notify changes on main thread
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                notifyDataSetChanged();
                Log.d(TAG, "notifyDataSetChanged() called on main thread");
            });
        } else {
            notifyDataSetChanged();
            Log.d(TAG, "notifyDataSetChanged() called");
        }

        Log.d(TAG, "Final size after update: " + this.recipeList.size());

        // Additional verification
        if (this.recipeList.size() > 0) {
            Log.d(TAG, "✓ Recipes successfully added to adapter");
            Log.d(TAG, "First recipe: " + this.recipeList.get(0).getName());
        } else {
            Log.w(TAG, "⚠ No recipes in adapter after update");
        }
    }

    public List<Recipe> getCurrentRecipes() {
        return recipeList;
    }
    public void refreshAdapter() {
        Log.d(TAG, "Manual refresh triggered, current size: " +
                (recipeList != null ? recipeList.size() : 0));
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView tvRecipeName;
        TextView tvCookingTime;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            ivRecipeImage = itemView.findViewById(R.id.iv_recipe_image);
            tvRecipeName = itemView.findViewById(R.id.tv_recipe_name);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);

            // Verify all views are found
            if (ivRecipeImage == null) Log.e("RecipeAdapter", "iv_recipe_image not found!");
            if (tvRecipeName == null) Log.e("RecipeAdapter", "tv_recipe_name not found!");
            if (tvCookingTime == null) Log.e("RecipeAdapter", "tv_cooking_time not found!");

            Log.d("RecipeAdapter", "ViewHolder created - all views: " +
                    (ivRecipeImage != null && tvRecipeName != null && tvCookingTime != null ? "OK" : "ERROR"));
        }
    }
}