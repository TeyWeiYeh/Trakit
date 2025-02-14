package data.network.controller;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;
import domain.Budget;

public class BudgetController {
    private ApiController apiController;
    private Context context;
    public BudgetController(Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }
    //functions to perform crud operations for budgets, with appropriate error handling
    public void getAllBudgets(ICallback callback){
        apiController.getAllBudgets(response -> {
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONArray data = new JSONArray(responseObject.optString("data"));
                callback.onSuccess(data);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error-> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve Budgets");
        });
    }

    public void createBudget(Budget budget, ICallback callback){
        apiController.createBudget(budget, response -> {
            callback.onSuccess("Successfully created budget");
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to create Budget");
        });
    }
    public void updateBudget(Budget budget, ICallback callback){
        apiController.updateBudget(budget, response -> {
            callback.onSuccess("Successfully updated budget");
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to update Budget");
        });
    }

    public void deleteBudget(String id, ICallback callback){
        apiController.deleteBudget(id, response -> {
            callback.onSuccess("Successfully deleted budget");
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to delete Budget");
        });
    }

    public void getAllBudgetsByDate(String date, ICallback callback){
        apiController.getAllBudgetsByDate(date, response -> {
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONArray data = new JSONArray(responseObject.optString("data"));
                callback.onSuccess(data);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error-> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve Budgets");
        });
    }
}
