package data.network.controller;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;
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
                callback.onSuccess(dataArray);
                System.out.println(dataArray);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error->{
            if (error instanceof AuthFailureError)
                callback.onError("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve transactions" + error.toString());
        });
    }
}
