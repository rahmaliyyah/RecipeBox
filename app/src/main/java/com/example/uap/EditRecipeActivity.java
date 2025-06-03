package com.example.uap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditRecipeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton btnBack, btnDeleteRecipe;
    private ImageView ivRecipeImageEdit;
    private LinearLayout layoutImageEditOverlay;
    private TextInputEditText etFoodNameEdit, etCookingTimeEdit, etIngredientsEdit, etHowToMakeEdit;
    private MaterialButton btnUpdateRecipe;

    private DatabaseReference databaseReference;
    private String recipeId;
    private Recipe currentRecipe;
    private Uri newImageUri;
    private boolean isImageChanged = false;
    private String newImageBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        recipeId = getIntent().getStringExtra("RECIPE_ID");

        if (recipeId == null) {
            Toast.makeText(this, "ID resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadRecipeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_edit);
        btnDeleteRecipe = findViewById(R.id.btn_delete_recipe);
        ivRecipeImageEdit = findViewById(R.id.iv_recipe_image_edit);
        layoutImageEditOverlay = findViewById(R.id.layout_image_edit_overlay);
        etFoodNameEdit = findViewById(R.id.et_food_name_edit);
        etCookingTimeEdit = findViewById(R.id.et_cooking_time_edit);
        etIngredientsEdit = findViewById(R.id.et_ingredients_edit);
        etHowToMakeEdit = findViewById(R.id.et_how_to_make_edit);
        btnUpdateRecipe = findViewById(R.id.btn_update_recipe);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDeleteRecipe.setOnClickListener(v -> showDeleteConfirmationDialog());

        layoutImageEditOverlay.setOnClickListener(v -> openImageChooser());

        btnUpdateRecipe.setOnClickListener(v -> updateRecipe());
    }

    private void loadRecipeData() {
        databaseReference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentRecipe = dataSnapshot.getValue(Recipe.class);
                if (currentRecipe != null) {
                    currentRecipe.setId(dataSnapshot.getKey());
                    populateFields();
                } else {
                    Toast.makeText(EditRecipeActivity.this,
                            "Resep tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditRecipeActivity.this,
                        "Gagal memuat data resep: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        etFoodNameEdit.setText(currentRecipe.getName());
        etCookingTimeEdit.setText(currentRecipe.getCookingTime());
        etIngredientsEdit.setText(currentRecipe.getIngredients());
        etHowToMakeEdit.setText(currentRecipe.getInstructions());

        // Load current image from Base64
        if (currentRecipe.getImageUrl() != null && !currentRecipe.getImageUrl().isEmpty()) {
            try {
                // Check if it's Base64 or URL
                if (currentRecipe.getImageUrl().startsWith("data:image") ||
                        currentRecipe.getImageUrl().matches("^[A-Za-z0-9+/]*={0,2}$")) {
                    // It's Base64
                    loadBase64Image(currentRecipe.getImageUrl());
                } else {
                    // It's URL (for backward compatibility)
                    Glide.with(this)
                            .load(currentRecipe.getImageUrl())
                            .placeholder(R.drawable.placeholder_food)
                            .error(R.drawable.placeholder_food)
                            .into(ivRecipeImageEdit);
                }
            } catch (Exception e) {
                ivRecipeImageEdit.setImageResource(R.drawable.placeholder_food);
            }
        } else {
            ivRecipeImageEdit.setImageResource(R.drawable.placeholder_food);
        }
    }

    private void loadBase64Image(String base64String) {
        try {
            String base64Image = base64String;
            if (base64String.startsWith("data:image")) {
                base64Image = base64String.substring(base64String.indexOf(",") + 1);
            }

            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivRecipeImageEdit.setImageBitmap(decodedByte);
        } catch (Exception e) {
            ivRecipeImageEdit.setImageResource(R.drawable.placeholder_food);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar Baru"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            newImageUri = data.getData();

            try {
                // Convert image to Base64
                InputStream inputStream = getContentResolver().openInputStream(newImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Resize bitmap to reduce size
                bitmap = resizeBitmap(bitmap, 800, 600);

                // Convert to Base64
                newImageBase64 = bitmapToBase64(bitmap);

                // Display the image
                ivRecipeImageEdit.setImageBitmap(bitmap);
                isImageChanged = true;

            } catch (Exception e) {
                Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void updateRecipe() {
        String foodName = etFoodNameEdit.getText().toString().trim();
        String cookingTime = etCookingTimeEdit.getText().toString().trim();
        String ingredients = etIngredientsEdit.getText().toString().trim();
        String howToMake = etHowToMakeEdit.getText().toString().trim();

        if (TextUtils.isEmpty(foodName)) {
            etFoodNameEdit.setError("Nama makanan harus diisi");
            etFoodNameEdit.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cookingTime)) {
            etCookingTimeEdit.setError("Waktu memasak harus diisi");
            etCookingTimeEdit.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ingredients)) {
            etIngredientsEdit.setError("Bahan-bahan harus diisi");
            etIngredientsEdit.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(howToMake)) {
            etHowToMakeEdit.setError("Cara membuat harus diisi");
            etHowToMakeEdit.requestFocus();
            return;
        }

        btnUpdateRecipe.setEnabled(false);
        btnUpdateRecipe.setText("Mengupdate...");

        String imageToSave = isImageChanged ? newImageBase64 : currentRecipe.getImageUrl();
        updateRecipeInDatabase(foodName, cookingTime, ingredients, howToMake, imageToSave);
    }

    private void updateRecipeInDatabase(String foodName, String cookingTime,
                                        String ingredients, String howToMake, String imageBase64) {
        currentRecipe.setName(foodName);
        currentRecipe.setCookingTime(cookingTime);
        currentRecipe.setIngredients(ingredients);
        currentRecipe.setInstructions(howToMake);
        currentRecipe.setImageUrl(imageBase64);

        databaseReference.child(recipeId).setValue(currentRecipe)
                .addOnCompleteListener(task -> {
                    btnUpdateRecipe.setEnabled(true);
                    btnUpdateRecipe.setText("Update Resep");

                    if (task.isSuccessful()) {
                        Toast.makeText(EditRecipeActivity.this,
                                "Resep berhasil diupdate!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditRecipeActivity.this,
                                "Gagal mengupdate resep: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
                        Toast.makeText(EditRecipeActivity.this,
                                "Resep berhasil dihapus!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditRecipeActivity.this,
                                "Gagal menghapus resep: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}