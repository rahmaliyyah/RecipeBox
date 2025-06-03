package com.example.uap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddRecipeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddRecipeActivity";
    private static final int MAX_IMAGE_SIZE = 500;

    private ImageButton btnBack;
    private LinearLayout layoutImageUpload;
    private ImageView ivRecipeImage;
    private LinearLayout layoutUploadPlaceholder;
    private TextInputEditText etFoodName, etCookingTime, etIngredients, etHowToMake;
    private MaterialButton btnSaveRecipe;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String base64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        layoutImageUpload = findViewById(R.id.layout_image_upload);
        ivRecipeImage = findViewById(R.id.iv_recipe_image);
        layoutUploadPlaceholder = findViewById(R.id.layout_upload_placeholder);
        etFoodName = findViewById(R.id.et_food_name);
        etCookingTime = findViewById(R.id.et_cooking_time);
        etIngredients = findViewById(R.id.et_ingredients);
        etHowToMake = findViewById(R.id.et_how_to_make);
        btnSaveRecipe = findViewById(R.id.btn_save_recipe);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        layoutImageUpload.setOnClickListener(v -> openImageChooser());
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            try {
                Uri imageUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Resize bitmap untuk mengurangi ukuran
                Bitmap resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE);

                // Convert ke Base64
                base64Image = bitmapToBase64(resizedBitmap);

                // Show image preview
                ivRecipeImage.setImageBitmap(resizedBitmap);
                ivRecipeImage.setVisibility(View.VISIBLE);
                layoutUploadPlaceholder.setVisibility(View.GONE);

                Log.d(TAG, "Image converted to Base64, size: " + base64Image.length());

            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxSize) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // 80% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveRecipe() {
        String foodName = etFoodName.getText().toString().trim();
        String cookingTime = etCookingTime.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String howToMake = etHowToMake.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Nama makanan harus diisi");
            etFoodName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cookingTime)) {
            etCookingTime.setError("Waktu memasak harus diisi");
            etCookingTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ingredients)) {
            etIngredients.setError("Bahan-bahan harus diisi");
            etIngredients.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(howToMake)) {
            etHowToMake.setError("Cara membuat harus diisi");
            etHowToMake.requestFocus();
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveRecipe.setEnabled(false);
        btnSaveRecipe.setText("Menyimpan...");

        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String recipeId = databaseReference.push().getKey();

        if (recipeId == null) {
            Toast.makeText(this, "Gagal membuat ID resep", Toast.LENGTH_SHORT).show();
            btnSaveRecipe.setEnabled(true);
            btnSaveRecipe.setText("Simpan Resep");
            return;
        }

        Recipe recipe = new Recipe(foodName, cookingTime, ingredients, howToMake, base64Image, currentUserId);

        databaseReference.child(recipeId).setValue(recipe)
                .addOnCompleteListener(task -> {
                    btnSaveRecipe.setEnabled(true);
                    btnSaveRecipe.setText("Simpan Resep");

                    if (task.isSuccessful()) {
                        Toast.makeText(AddRecipeActivity.this,
                                "Resep berhasil ditambahkan!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddRecipeActivity.this,
                                "Gagal menyimpan resep: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}