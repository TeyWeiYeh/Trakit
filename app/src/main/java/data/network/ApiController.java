package data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import domain.Category;
import domain.Transaction;
import domain.User;
import mdad.localdata.trakit.MainActivity;

public class ApiController {
    private final Context context;
    public ApiController(Context context) {
        this.context = context;
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    String user_url = MainActivity.ipBaseUrl + "/user.php";
    String cat_url = MainActivity.ipBaseUrl + "/category.php";
    String trans_url = MainActivity.ipBaseUrl + "/transaction.php";

    //User API's
    public void getUserDetails(String id, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/user.php?id=" + id;
        StringRequest getUserDetailsRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getUserDetailsRequest);
    }
    public void updateUserDetails(User user, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = user_url + "?id=" + user.id;
        JSONObject updateUserObject = new JSONObject();
        try{
            updateUserObject.put("email", user.email);
            updateUserObject.put("profile_pic", user.base64_profile_pic);
            JsonObjectRequest updateUserRequest = new JsonObjectRequest(Request.Method.PUT, url, updateUserObject, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders(){
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(updateUserRequest);
        } catch (JSONException e){
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("JSON error: " + e.getMessage()));
        }
    }

    //Category API's
    public void getAllCategory(String type, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/category.php?type=" + type;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public void createCategory(Category category, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/category.php";
        String token = getStoredToken();
        try{
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JSONObject payloadJson = new JSONObject(payload);
            String userId = payloadJson.optString("user_id"); //prevents exceptions
            if (userId.isEmpty()) {
                throw new JSONException("userId not found in token payload");
            }
            JSONObject requestBody = new JSONObject();
            requestBody.put("name",category.name);
            requestBody.put("type",category.getType().toString());
            requestBody.put("userId", userId);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError{
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token); // Add Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
        }catch(JSONException e){
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("JSON error: " + e.getMessage()));
        }
    }
    public void updateCategory(String id, Category category, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/category.php?id=" + id;
        JSONObject requestBody = new JSONObject();
        try{
            requestBody.put("name",category.name);
            requestBody.put("type",category.getType().toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, requestBody, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e){
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("Volley error: " + e.getMessage()));
        }
    }
    public void deleteCategory(String id, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/category.php?id=" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }





    //Transaction API
    public void getAllTransactions(String monthYear, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/transaction.php?date_created=" + monthYear;
        StringRequest getAllTransactionRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getAllTransactionRequest);
    }
    public void createTransaction(Transaction transaction, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        JSONObject transObject = new JSONObject();
        Date date;
        String formattedDate;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = inputFormat.parse(transaction.transDate);
            formattedDate = outputFormat.format(date);
            transObject.put("amount", transaction.amount);
            transObject.put("description", transaction.description);
            transObject.put("trans_date", formattedDate);
            transObject.put("recurring", transaction.recurring);
            transObject.put("category_id", transaction.categoryId);
            transObject.put("image", transaction.base64Img);
            JsonObjectRequest createTransObject = new JsonObjectRequest(Request.Method.POST, trans_url, transObject, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders(){
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(createTransObject);
        } catch (Exception e){
            errorListener.onErrorResponse(new VolleyError("JSON error"));
        }
    }

    public void updateTransaction(Transaction transaction, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = trans_url + "?id=" + transaction.id;
        Date date;
        String formattedDate;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = inputFormat.parse(transaction.transDate);
            formattedDate = outputFormat.format(date);

            JSONObject transObject = new JSONObject();
            try {
                transObject.put("amount", transaction.amount);
                transObject.put("description", transaction.description);
                transObject.put("trans_date", formattedDate);
                transObject.put("recurring", transaction.isRecurring());
                transObject.put("category_id", transaction.categoryId);
                transObject.put("image", transaction.base64Img);
                JsonObjectRequest createTransObject = new JsonObjectRequest(Request.Method.PUT, url, transObject, successListener, errorListener){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + getStoredToken());
                        return headers;
                    }
                };
                VolleySingleton.getInstance(context).addToRequestQueue(createTransObject);
            } catch (JSONException e){
                e.printStackTrace();
                errorListener.onErrorResponse(new VolleyError("JSON error"));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void deleteTransaction(String id, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = trans_url + "?id=" + id;
        StringRequest deleteTransRequest = new StringRequest(Request.Method.DELETE, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(deleteTransRequest);
    }

    private String getStoredToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }
}
