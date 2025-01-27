package mdad.localdata.trakit.mainfragments;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import data.network.ICallback;
import data.network.controller.CategoryController;
import data.network.controller.TransactionController;
import domain.Category;
import domain.Transaction;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.ProfileActivity;
import mdad.localdata.trakit.R;
import utils.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFragment newInstance(String param1, String param2) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    RadioGroup radioGroup;
    RadioButton expenseButton, incomeButton;
    EditText etCreateDate, etCreateAmount, etCreateDesc;
    MaterialToolbar topAppBar;
    CategoryController categoryController;
    TransactionController transactionController;
    AutoCompleteTextView catDropdownValue;
    SharedPreferences sharedPreferences;
    ImageView transImg;
    Bitmap selectedImageBitmap = null;
    Button btnAdd, btnChooseImage, btnViewImage, closeButton;
    ImageButton btnRecurring;
    String token, currentPhotoPath, base64Img, catId;
    String type = Category.Type.EXPENSE.toString();
    ArrayList<HashMap<String, String>> arrayList;
    Boolean isCreated, recurring = false;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    public void onViewCreated(View view, Bundle savedInstanceState){
        categoryController = new CategoryController(getContext());
        transactionController = new TransactionController(getContext());
        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);

        topAppBar = view.findViewById(R.id.topAppBar);
        etCreateAmount = view.findViewById(R.id.etCreateAmount);
        etCreateDesc = view.findViewById(R.id.etCreateDesc);
        etCreateDate = view.findViewById(R.id.etCreateDate);
        catDropdownValue = view.findViewById(R.id.selectDropdown);
        radioGroup = view.findViewById(R.id.radioGroup);
        expenseButton = view.findViewById(R.id.expenseButton);
        incomeButton = view.findViewById(R.id.incomeButton);
        btnAdd = view.findViewById(R.id.btnCreate);
        btnChooseImage = view.findViewById(R.id.btnCreateImage);
        btnViewImage = view.findViewById(R.id.btnViewImage);
        btnRecurring = view.findViewById(R.id.btnRecurring);

        topAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_profile) {
                    // Navigate to profile
                    Intent goToProfilePage = new Intent(getContext(), ProfileActivity.class);
                    startActivity(goToProfilePage);
                    return true;
                } else if (itemId == R.id.action_logout) {
                    // Logout logic
                    sharedPreferences.edit().putString("token",null).apply();
                    Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                    //clears the stack
                    goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(goToLoginPage);
                    return true;
                } else
                    return false;
            }
        });
        radioGroup.check(R.id.expenseButton);
        getCategoriesByType(type);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.expenseButton) {
                expenseButton.setBackgroundResource(R.drawable.rounded_left_selected);
                incomeButton.setBackgroundResource(R.drawable.rounded_right_unselected);
                type = Category.Type.EXPENSE.toString();
            } else if (checkedId == R.id.incomeButton) {
                incomeButton.setBackgroundResource(R.drawable.rounded_right_selected);
                expenseButton.setBackgroundResource(R.drawable.rounded_left_unselected);
                type = Category.Type.INCOME.toString();
            }
            getCategoriesByType(type);
        });
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        etCreateDate.setFocusable(false);  // Make the EditText non-editable (so user clicks to trigger date picker)
        etCreateDate.setClickable(true);   // Make the EditText clickable to trigger date picker

        etCreateDate.setOnClickListener(v -> {
            materialDatePicker.show(getParentFragmentManager(), materialDatePicker.getTag());  // Show the date picker on click
        });

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected date (milliseconds) to a formatted date string
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = sdf.format(calendar.getTime());
            etCreateDate.setText(formattedDate);  // Set the formatted date into EditText
        });

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSourceDialog();
            }
        });
        btnViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullScreenDialog();
            }
        });
        btnRecurring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnRecurring.setColorFilter(ContextCompat.getColor(getContext(),R.color.save));
                recurring = !recurring;
                if (!recurring){
                    btnRecurring.setColorFilter(ContextCompat.getColor(getContext(),R.color.recurring));
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = etCreateAmount.getText().toString();
                String desc = etCreateDesc.getText().toString();
                String date = etCreateDate.getText().toString();
                String catName = catDropdownValue.getText().toString().toLowerCase();
                catId = null; // Initialize to null by default
                for (int i = 0; i < arrayList.size(); i++) {
                    String name = arrayList.get(i).get("name").toLowerCase(); // Get the name from the arrayList
                    String id = arrayList.get(i).get("id"); // Get the id from the arrayList
                    if (catName.equals(name)) { // Check if the dropdown value matches
                        catId = id; // Set the newCatId
                        break; // Exit the loop once a match is found
                    }
                }
                if (amt.isEmpty() || desc.isEmpty() || date.isEmpty() || catId == null){
                    Toast.makeText(getContext(),"Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                else{
                    Transaction newTransObject = new Transaction(Float.parseFloat(amt), desc, date, recurring, catId, base64Img);
                    createTransaction(newTransObject);
                }
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image Source");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Take Photo
                    takePhoto();
                } else if (which == 1) {
                    // Choose from Gallery
                    pickFromGallery();
                }
            }
        });
        builder.show();
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                System.out.print("Error creating file path");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "mdad.localdata.trakit", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }

        } else {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    selectedImageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
                // Handle the photo taken by the camera
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Handle the photo selected from the gallery
                Uri selectedImageUri = data.getData();
                try {
                    // Get the selected image as a Bitmap
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //Bitmap resizeImg = ImageUtils.editImgSize(selectedImageBitmap, 380, 600);
            Bitmap resizeImg = ImageUtils.scaleToTargetSize(selectedImageBitmap, 100 * 1024, Bitmap.CompressFormat.PNG);
            base64Img = ImageUtils.encodeToBase64(resizeImg, Bitmap.CompressFormat.PNG, 100);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MDAD_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, /* prefix */ ".jpg", /* suffix */ storageDir /* directory */);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showFullScreenDialog() {
        Dialog dialog = new Dialog(getContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_image, null);

        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        transImg = dialogView.findViewById(R.id.transImg);

        if (selectedImageBitmap != null) transImg.setImageBitmap(selectedImageBitmap);
        else {
            transImg.setImageBitmap(null);
            isCreated = false;
        }

        closeButton = dialogView.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void getCategoriesByType(String type){
        categoryController.getAllCategories(type, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                arrayList = new ArrayList<>();
                ArrayList<String> allCategories = new ArrayList<>();
                try{
                    JSONArray dataArray = (JSONArray) result;
                    for (int i=0; i<dataArray.length();i++){
                        JSONObject item = dataArray.getJSONObject(i);
                        String id = item.getString("id");
                        String name = item.getString("name");
                        String type = item.getString("type");
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("name", name);
                        hashMap.put("type", type);
                        arrayList.add(hashMap);
                        allCategories.add(name);
                    }
                    Log.d("Array List", String.valueOf(arrayList));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, allCategories);
                    catDropdownValue.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error parsing category data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                catDropdownValue.setText("Error fetching categories");
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }

    public void createTransaction(Transaction transaction){
        transactionController.createTransaction(transaction, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                etCreateAmount.setText("");
                etCreateDesc.setText("");
                etCreateDate.setText("");
                catDropdownValue.setText("Select a category");
                getCategoriesByType(type);
                isCreated = true;
                recurring = false;
                selectedImageBitmap = null;
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }
}