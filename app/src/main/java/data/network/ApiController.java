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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import domain.Budget;
import domain.Category;
import domain.MonthlyReport;
import domain.Transaction;
import domain.User;
import mdad.localdata.trakit.MainActivity;
import utils.FileUtils;

public class ApiController {
    private final Context context;
    public ApiController(Context context) {
        this.context = context;
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    String user_url = MainActivity.ipBaseUrl + "/user.php";
    String cat_url = MainActivity.ipBaseUrl + "/category.php";
    String trans_url = MainActivity.ipBaseUrl + "/transaction.php";
    String budget_url = MainActivity.ipBaseUrl + "/budget.php";
    String wallet_url = MainActivity.ipBaseUrl + "/wallet.php";
    String monthlyReport_url = MainActivity.ipBaseUrl + "/monthlyReport.php";

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
        String url = trans_url + "?date_created=" + monthYear;
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
                transObject.put("recurring", transaction.recurring);
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

    public void getTransactionsByCatId(String catId, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = trans_url + "?transactions_for=" + catId;
        StringRequest getTransForCatRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getTransForCatRequest);
    }

    public void getAllTransactionsByDate(String day, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = trans_url + "?day=" + day;
        StringRequest getRecurringTransRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getRecurringTransRequest);
    }

    public void getAllRecurringTransactions(String monthYear, Boolean recurring, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = trans_url + "?date_created=" + monthYear + "&recurring=" + recurring;
        StringRequest getAllRecurringTransactionsRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getAllRecurringTransactionsRequest);
    }

    public void getLineChartData(String year, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/linechart.php?year=" + year;
        StringRequest lineChartDataRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(lineChartDataRequest);
    }

    public void getPieChartData(String year, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = MainActivity.ipBaseUrl + "/piechart.php?year=" + year;
        StringRequest lineChartDataRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(lineChartDataRequest);
    }

    public void getAllBudgets(Response.Listener<String> successListener, Response.ErrorListener errorListener){
        StringRequest getAllBudgetsRequest = new StringRequest(Request.Method.GET, budget_url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getAllBudgetsRequest);
    }

    public void createBudget(Budget budget, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        JSONObject budgetObject = new JSONObject();
        Date start_date, end_date;
        String formattedStartDate, formattedEndDate;
        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            start_date = inputFormat.parse(budget.start_date);
            end_date = inputFormat.parse(budget.end_date);
            formattedStartDate = outputFormat.format(start_date);
            formattedEndDate = outputFormat.format(end_date);
            budgetObject.put("name", budget.name);
            budgetObject.put("start_date", formattedStartDate);
            budgetObject.put("end_date", formattedEndDate);
            budgetObject.put("limit", budget.limit);
            JsonObjectRequest createBudgetObjectRequest = new JsonObjectRequest(Request.Method.POST, budget_url, budgetObject, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(createBudgetObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("JSON error"));
        }
    }

    public void updateBudget(Budget budget, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        String url = budget_url + "?id=" + budget.id;
        Date start_date, end_date;
        String formattedStartDate, formattedEndDate;
        JSONObject updatedBudgetObject = new JSONObject();
        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            start_date = inputFormat.parse(budget.start_date);
            end_date = inputFormat.parse(budget.end_date);
            formattedStartDate = outputFormat.format(start_date);
            formattedEndDate = outputFormat.format(end_date);
            updatedBudgetObject.put("name", budget.name);
            updatedBudgetObject.put("start_date", formattedStartDate);
            updatedBudgetObject.put("end_date", formattedEndDate);
            updatedBudgetObject.put("limit", budget.limit);
            JsonObjectRequest updateBudgetObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, updatedBudgetObject, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(updateBudgetObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
            errorListener.onErrorResponse(new VolleyError("JSON error"));
        }
    }

    public void deleteBudget(String id, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = budget_url + "?id=" + id;
        StringRequest deleteBudgetRequest = new StringRequest(Request.Method.DELETE, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(deleteBudgetRequest);
    }

    public void getWalletData(String monthYear, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = wallet_url + "?date=" + monthYear;
        StringRequest getWalletDataRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getWalletDataRequest);
    }

    public void getMonthlyReportData(String monthYear, Response.Listener<String> successListener, Response.ErrorListener errorListener){
        String url = monthlyReport_url + "?date=" + monthYear;
        StringRequest getMonthlyReportRequest = new StringRequest(Request.Method.GET, url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getMonthlyReportRequest);
    }

    public void getAllReports(Response.Listener<String> successListener, Response.ErrorListener errorListener){
        StringRequest getALlReportsRequest = new StringRequest(Request.Method.GET, monthlyReport_url, successListener, errorListener){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getStoredToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(getALlReportsRequest);
    }

    public void createMonthlyReport(MonthlyReport report, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        JSONObject reportObject = new JSONObject();
        String base64ExcelFile = FileUtils.encodeFileToBase64(report.excelFile);
        try{
            reportObject.put("date", report.monthYear);
            reportObject.put("base64file", base64ExcelFile);
            reportObject.put("name", report.name);
            JsonObjectRequest reportObjectRequest = new JsonObjectRequest(Request.Method.POST, monthlyReport_url, reportObject, successListener, errorListener){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getStoredToken());
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(reportObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getStoredToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }
}
