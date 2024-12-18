package mdad.localdata.trakit.mainfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data.network.VolleySingleton;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.MainActivity;
import mdad.localdata.trakit.R;
import mdad.localdata.trakit.authfragments.LoginFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
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
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    MaterialToolbar topAppBar;
    String url = MainActivity.ipBaseUrl + "/category/create.php?type=";
    String token;
    ListView category_list;
    String type = "Expense";
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        topAppBar = view.findViewById(R.id.topAppBar);
        FragmentManager fm = getParentFragmentManager();
        topAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.action_profile) {
                    // Navigate to profile
                    Toast.makeText(requireContext().getApplicationContext(), "Profile clicked", Toast.LENGTH_LONG).show();
                    return true;
                } else if (itemId == R.id.action_logout) {
                    // Logout logic
                    sharedPreferences.edit().putString("token","").apply();
                    Intent i = new Intent(getContext(), AuthActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton expenseButton = view.findViewById(R.id.expenseButton);
        RadioButton incomeButton = view.findViewById(R.id.incomeButton);
        category_list = view.findViewById(R.id.category_list);
        radioGroup.check(R.id.expenseButton);
        GetAllCategory(url, type);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.expenseButton) {
                expenseButton.setBackgroundResource(R.drawable.rounded_left_selected);
                incomeButton.setBackgroundResource(R.drawable.rounded_right_unselected);
                System.out.println(token);
                type="Expense";
                GetAllCategory(url, type);
            } else if (checkedId == R.id.incomeButton) {
                incomeButton.setBackgroundResource(R.drawable.rounded_right_selected);
                expenseButton.setBackgroundResource(R.drawable.rounded_left_unselected);
                type = "Income";
                GetAllCategory(url, type);
            }
        });
    }

    public void GetAllCategory(String url, String type){
        ArrayList<HashMap<String,String>> arrayList=new ArrayList<>();
        RequestQueue requestQueue = VolleySingleton.getInstance(requireContext()).getRequestQueue();
        url = url + type;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String data = jsonObject.getString("data");
                    JSONArray dataArray = new JSONArray(data);
                    //Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    System.out.println(dataArray);
                    for (int i=0; i<dataArray.length();i++){
                        JSONObject item = dataArray.getJSONObject(i);
                        String id = item.getString("id");
                        String name = item.getString("name");
                        String type = item.getString("type");
                        HashMap<String,String> hashMap=new HashMap<>();
                        hashMap.put("name",name);
                        hashMap.put("type",type);
                        arrayList.add(hashMap);
                    }
                    String[] from={"name","type"};//string array
                    int[] to={R.id.tvName,R.id.tvType};//int array of views id's
                    SimpleAdapter simpleAdapter=new SimpleAdapter(getContext(),arrayList,R.layout.list_view_items,from,to);
                    category_list.setAdapter(simpleAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Response error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                // Add the Authorization header here
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token); // Add the Bearer token
                headers.put("type","Expense");
                return headers;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}