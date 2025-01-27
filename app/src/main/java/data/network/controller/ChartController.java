package data.network.controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONObject;

import data.network.ApiController;
import data.network.ICallback;

public class ChartController {
    private ApiController apiController;
    private Context context;
    public ChartController(Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }

    public void getLineChartData(String year, ICallback callback){
        apiController.getLineChartData(year, response->{
            try{
                JSONObject jsonObject = new JSONObject(response);
                JSONObject dataObject = new JSONObject(jsonObject.optString("data"));
                callback.onSuccess(dataObject);
            } catch (Exception e){
                e.printStackTrace();
                callback.onError("JSON error: " + e.getMessage());
            }
        }, error->{
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again.");
            else
                callback.onError("Failed to fetch chart data");
        });
    }

    public void getPieChartData(String year, ICallback callback){
        apiController.getPieChartData(year, response -> {
            try{
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.isNull("data")) {
                    callback.onSuccess(null);
                } else{
                    JSONArray dataObject = new JSONArray(jsonObject.optString("data"));
                    callback.onSuccess(dataObject);
                }
            } catch (Exception e){
                e.printStackTrace();
                callback.onError("JSON error: " + e.getMessage());
            }
        }, error-> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again.");
            else
                callback.onError("Failed to fetch chart data");
        });
    }
}
