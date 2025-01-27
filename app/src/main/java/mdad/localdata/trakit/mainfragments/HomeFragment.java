package mdad.localdata.trakit.mainfragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import data.network.VolleySingleton;
import mdad.localdata.trakit.R;
import workers.RecurringBillWorker;
//import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    Button btnSendQuery;
    EditText etQuery;
    TextView tvResponse;
    private final String url = "https://api.deepseek.com/chat/completions";
    ArrayList<Map<String,String>> chat;
    JSONArray contentArray = new JSONArray();
    public void onViewCreated(View view, Bundle savedInstanceState){
//            btnSendQuery = view.findViewById(R.id.btnSendQuery);
//            etQuery = view.findViewById(R.id.etQuery);
//            tvResponse = view.findViewById(R.id.tvResponse);
//            chat = new ArrayList<>();
//            try{
//            } catch (Exception e){
//                Log.d("JSON obj", e.toString());
//            }
//            btnSendQuery.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String query = etQuery.getText().toString();
//                    getResponse(query);
//                }
//            });
        PeriodicWorkRequest fetchRecurringBills = new PeriodicWorkRequest.Builder(
                RecurringBillWorker.class,
                1, TimeUnit.DAYS
        ).build();
        WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
                "RecurringBillWorker", // Unique name for the worker
                ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
                fetchRecurringBills
        );
    }


    private void getResponse(String query){

        RequestQueue requestQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject userQuery = new JSONObject();
            userQuery.put("role", "user");
            userQuery.put("content", query);
            contentArray.put(userQuery);
            // Adding params to JSON object
            jsonObject.put("model", "deepseek-chat");
            jsonObject.put("messages", contentArray);
            jsonObject.put("temperature", 1.3);
            jsonObject.put("max_tokens", 20);
            Log.d("json", String.valueOf(contentArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Getting response message and setting it to text view
                    String responseMsg = response
                            .getJSONArray("choices")      // Navigate to the "choices" array
                            .getJSONObject(0)            // Get the first object in the array
                            .getJSONObject("message")    // Get the "message" object
                            .getString("content");       // Extract the "content" string
                    String role = response
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("role");
                    JSONObject respObj = new JSONObject();
                    respObj.put("role", role);
                    respObj.put("content", responseMsg);
                    Log.d("Response", responseMsg);
                    contentArray.put(respObj);
                    Log.d("chat", String.valueOf(contentArray));
                    tvResponse.setText(responseMsg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAGAPI", "Error is : " + error.getMessage() + "\n" + error);
            }
        }){
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                // Adding headers
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer sk-e0760c05d9c34feb9328b07be245d500");
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}