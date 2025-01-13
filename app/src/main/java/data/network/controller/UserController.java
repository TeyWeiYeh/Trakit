package data.network.controller;

import android.content.Context;

import com.android.volley.AuthFailureError;

import org.json.JSONException;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;
import domain.User;

public class UserController {
    private ApiController apiController;
    private Context context;

    public UserController (Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }

    public void getUserDetails(String id, ICallback callback){
        apiController.getUserDetails(id, response->{
            try{
                JSONObject userDetailsObject = new JSONObject(response);
                callback.onSuccess(userDetailsObject);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onError("JSON error" + e.getMessage());
            }
        }, error -> {
            if (error instanceof AuthFailureError)
                // Notify the callback about the authentication failure
                callback.onAuthFailure("Authentication failed. Please login again.");
            else
                callback.onError("Failed to retrieve user: " + error.toString());
        });
    }

    public void updateUserDetails(User user, ICallback callback){
        apiController.updateUserDetails(user, response -> {
            callback.onSuccess("User updated successfully");
        }, error -> {
            if (error instanceof AuthFailureError)
                // Notify the callback about the authentication failure
                callback.onAuthFailure("Authentication failed. Please log in again.");
            else
                callback.onError("Failed to update user:  " + error.getMessage());
        });
    }
}
