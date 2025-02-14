package data.network.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;
import utils.DateUtils;

public class WalletController {
    ApiController apiController;
    Context context;
    public WalletController(Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }

    //get the wallet data that can be filtered base on each month
    public void getWalletData(String monthYear, ICallback callback){
        String formattedMonthYear = DateUtils.convertToYearMonth(monthYear);
        apiController.getWalletData(formattedMonthYear, response -> {
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONObject dataObject = new JSONObject(responseObject.optString("data"));
                callback.onSuccess(dataObject);
            } catch(Exception e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error-> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve Wallet Data");
        });
    }
}
