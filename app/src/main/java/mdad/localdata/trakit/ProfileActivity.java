package mdad.localdata.trakit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import data.network.ICallback;
import data.network.controller.UserController;
import domain.User;
import utils.ImageUtils;

public class ProfileActivity extends AppCompatActivity {
    ImageView ivProfilePic;
    Button btnChangeProfilePic, btnUpdate, btnLogout;
    UserController userController;
    SharedPreferences sharedPreferences;
    String token, user_id, base64ImgString;
    EditText etProfileEmail, etProfileUsername;
    MaterialToolbar topAppBar;
//    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userController = new UserController(getApplicationContext());

        sharedPreferences = getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        try{
            String[] chunks = token.split("\\.");
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JSONObject payloadJson = new JSONObject(payload);
            user_id = payloadJson.optString("user_id");
            getUserDetails(user_id);
        } catch (JSONException e){
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        btnChangeProfilePic = (Button) findViewById(R.id.btnChangeProfilePic);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        etProfileEmail = (EditText) findViewById(R.id.etProfileEmail);
        etProfileUsername = (EditText) findViewById(R.id.etProfileUsername);
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBackToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(goBackToMainActivity);
                finish();
            }
        });
        btnChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User updateUserObject = new User(user_id, etProfileEmail.getText().toString(), base64ImgString);
                updateUserDetails(updateUserObject);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserDetails(user_id);
            }
        });
    }

    private void openGallery() {
        // Create an intent to open the camera
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, CAMERA_CAPTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Get the selected image as a Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap = ImageUtils.editImgSize(bitmap, 120, 120);
                base64ImgString = ImageUtils.encodeToBase64(bitmap, Bitmap.CompressFormat.PNG, 100);
                // Set the image to an ImageView
                ivProfilePic.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getUserDetails(String id){
        userController.getUserDetails(id, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                try {
                    JSONObject userDetails = (JSONObject) result;
                    JSONObject data = userDetails.getJSONObject("data");
                    String email = data.optString("email", null);
                    String username = data.optString("username", null);
                    String base64_profile_pic = data.optString("profile_pic", null);
//                    Bitmap bitmap_profile_pic = decodeBase64(base64_profile_pic);
//                    ivProfilePic.setImageBitmap(bitmap_profile_pic);
                    etProfileEmail.setText(email);
                    etProfileUsername.setText(username);
                    if (!base64_profile_pic.isEmpty()) {
                        Bitmap bitmap_profile_pic = ImageUtils.decodeBase64(base64_profile_pic);
                        if (bitmap_profile_pic != null) {
                            ivProfilePic.setImageBitmap(bitmap_profile_pic);
                        } else {
                            // Handle the error or set a default image
                            ivProfilePic.setImageResource(R.drawable.profile); // Replace with your default image
                        }
                    } else {
                        ivProfilePic.setImageResource(R.drawable.profile); // Replace with your default image
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String error) {
                System.out.println("Error: " + error);
            }
            @Override
            public void onAuthFailure(String message) {
                Intent toLoginPage = new Intent(getApplicationContext(), AuthActivity.class);
                toLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(toLoginPage);
            }
        });
    }

    private void updateUserDetails(User user){
        user.base64_profile_pic = base64ImgString;
        userController.updateUserDetails(user, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getApplicationContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }

//    private String convertBitmapToBase64(Bitmap bitmap) {
//        if (bitmap == null) {
//            throw new IllegalArgumentException("Bitmap is null");
//        }
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        if (!success) {
//            throw new RuntimeException("Failed to compress bitmap");
//        }
//        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(byteArray, Base64.NO_WRAP); // Use NO_WRAP for consistency
//    }
//
//    public Bitmap convertBase64ToBitmap(String base64String) {
//        if (base64String == null || base64String.isEmpty()) {
//            throw new IllegalArgumentException("Base64 string is null or empty");
//        }
//        try {
//            byte[] decodedString = Base64.decode(base64String, Base64.NO_WRAP); // Use NO_WRAP for consistency
//            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            System.out.println("Failed to decode Base64 string");
//            return null;
//        }
//    }

}