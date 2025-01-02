package data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import domain.Category;

public class ApiController {
    private static final String base_url = "http://192.168.18.18/project/api";
    private final Context context;
    public ApiController(Context context) {
        this.context = context;
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    //Category API's
    public void getAllCategory(String type, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = base_url + "/category.php?type=" + type;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add your JWT token
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public void createCategory(String token, Category category, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = base_url + "/category.php";
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
            errorListener.onErrorResponse(new VolleyError("Volley error: " + e.getMessage()));
        }
    }
    public void updateCategory(String id, Category category, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = base_url + "/category.php?id=" + id;
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
        String url = base_url + "/category.php?id=" + id;
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
    private String getStoredToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }
}
