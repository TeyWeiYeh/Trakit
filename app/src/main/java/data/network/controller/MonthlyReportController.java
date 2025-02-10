package data.network.controller;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import data.network.ApiController;
import data.network.ICallback;
import domain.MonthlyReport;
import utils.DateUtils;
import utils.FileUtils;

public class MonthlyReportController {
    private ApiController apiController;
    private Context context;
    public MonthlyReportController(Context context){
        this.context = context;
        this.apiController = new ApiController(context);
    }

    public void getMonthlyReportData(String monthYear, ICallback callback){
        apiController.getMonthlyReportData(monthYear, response-> {
            try{
                JSONObject responseObject = new JSONObject(response);
                JSONArray dataArray = new JSONArray(responseObject.optString("data"));
                callback.onSuccess(dataArray);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(context, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error->{
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to retrieve data " + error.toString());
        });
    }

    public void getAllReports(ICallback callback){
        apiController.getAllReports(response -> {
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
                callback.onError("Failed to get reports" + error.toString());
        });
    }

    public void createMonthlyReport(MonthlyReport report, ICallback callback){
        apiController.createMonthlyReport(report, response -> {
            callback.onSuccess("Monthly report generated");
        }, error -> {
            if (error instanceof AuthFailureError)
                callback.onAuthFailure("Authentication failed. Please login again");
            else
                callback.onError("Failed to generate report " + error.toString());
        });
    }
}
