package data.network.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;
import domain.Transaction;
import utils.DateUtils;

public class TransactionController {
    private ApiController apiController;
    private Context context;
    public TransactionController(Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }

    public void getAllTransactions(String monthYear, ICallback callback){
        String formattedMonthYear = DateUtils.convertToYearMonth(monthYear);
        apiController.getAllTransactions(formattedMonthYear, response->{
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONArray dataArray = new JSONArray(responseObject.optString("data"));
                Log.d("count of array", String.valueOf(dataArray.length()));
                callback.onSuccess(dataArray);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error->{
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve transactions" + error.toString());
        });
    }

    public void createTransaction(Transaction transaction, ICallback callback){
        apiController.createTransaction(transaction, response -> {
            callback.onSuccess("Successfully created transaction");
        }, error -> {
            if (error instanceof AuthFailureError){
                callback.onAuthFailure("Authentication failed. Please login again");
            }
            else
                callback.onError("Failed to create transaction");
        });
    }

    public void updateTransaction(Transaction transaction, ICallback callback){
        apiController.updateTransaction(transaction, response->{
            callback.onSuccess("Successfully updated transaction");
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to update transaction");
        });
    }

    public void deleteTransaction(String id, ICallback callback){
        apiController.deleteTransaction(id, response->{
            callback.onSuccess("Successfully deleted transaction");
        }, error->{
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to delete transaction");
        });
    }

    public void getAllTransactionsByDate(String day, ICallback callback){
        apiController.getAllTransactionsByDate(day, response->{
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONArray dataArray = new JSONArray(responseObject.optString("data"));
                callback.onSuccess(dataArray);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve transactions");
        });
    }
}
