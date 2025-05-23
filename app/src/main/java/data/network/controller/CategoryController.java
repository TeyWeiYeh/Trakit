package data.network.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import data.network.ApiController;
import data.network.ICallback;
import domain.Category;

public class CategoryController {
    private ApiController apiController;
    private Context context;
    public CategoryController(Context context) {
        this.context = context;
        this.apiController = new ApiController(context);
    }

    //functions to perform crud operations for categories, with appropriate error handling
    public void getAllCategories(String type, ICallback callback){
        apiController.getAllCategory(type, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray dataArray = new JSONArray(jsonObject.optString("data"));
                callback.onSuccess(dataArray);
            } catch (JSONException e){
                e.printStackTrace();
                callback.onError("JSON error: " + e.getMessage());
            }
        }, error -> {
            if (error instanceof AuthFailureError)
                // Notify the callback about the authentication failure
                callback.onAuthFailure("Authentication failed. Please login again.");
            else
                callback.onError("Failed to retrieve categories" + error.toString());
        });
    }

    public void createCategory(Category category, ICallback callback) {
        apiController.createCategory(category, response -> {
            callback.onSuccess("Category created successfully");
        }, error -> {
            if (error instanceof AuthFailureError) {
                callback.onAuthFailure("Authentication failed. Please log in again.");
            }
            if (error.networkResponse!= null){
                if (error.networkResponse.statusCode == 409) {
                    callback.onError("Category already exist");
                }
                else {
                    callback.onError("Failed to create category");
                }
            }
            else {
                callback.onError("Server error");
            }
        });
    }

    public void updateCategory(String id, Category category, ICallback callback){
        apiController.updateCategory(id, category, response -> {
            callback.onSuccess("Category updated successfully");
        }, error -> {
            if (error instanceof AuthFailureError)
                // Notify the callback about the authentication failure
                callback.onAuthFailure("Authentication failed. Please log in again.");
            else
                callback.onError("Volley error: " + error.getMessage());
        });
    }

    public void deleteCategory(String id, ICallback callback){
        apiController.deleteCategory(id, response -> {
            callback.onSuccess("Category deleted successfully");
        }, error -> {
            callback.onError("Failed to delete Category");
        });
    }
}