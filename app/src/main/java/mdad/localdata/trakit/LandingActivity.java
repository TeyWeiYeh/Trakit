package mdad.localdata.trakit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import data.network.VolleySingleton;

public class LandingActivity extends AppCompatActivity {
    public static ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private static final int NUM_PAGES = 2;
    String token;
    SharedPreferences sharedPreferences;
    String url = MainActivity.ipBaseUrl + "/verify_token.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        if (token != null) {
            // Token exists, verify the token expiration
            verifyToken(url);
        }
        //else go login
        else {
            Intent i = new Intent(getApplicationContext(), AuthActivity.class);
            startActivity(i);
            finish();
        }
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = (ViewPager2) findViewById(R.id.mypager);
        pagerAdapter = new LandingActivity.MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }
    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(FragmentActivity fa) {
            super(fa);
        }
        @Override
        // to return the selected fragment depending on which tab is selected
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: {
                    //return LogoFragment.newInstance("fragment logo",null);
                    return SSFragment.newInstance("fragment ss", null);
                }
                case 1: {
                    //return LogoFragment.newInstance("fragment logo",null);
                }
                default:
                    return new Fragment();
            }
        }
        @Override
        // return count of tab items
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
    public void verifyToken(String url) {
        RequestQueue requestQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

        StringRequest verifyTokenRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String message = jsonObject.optString("message", ""); // Use optString with default null
                            Intent intent;// Close the current activity
                            if (message.equals("Valid token")) {
                                // Token is valid, navigate to MainActivity
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                            } else {
                                // Token is invalid or expired, navigate to AuthActivity
                                intent = new Intent(getApplicationContext(), AuthActivity.class);
                            }
                            startActivity(intent);
                            finish(); // Close the current activity
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getApplicationContext(), "Request failed: " + volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Add Authorization header
                return headers;
            }
        };

        // Add the request to the Volley queue
        requestQueue.add(verifyTokenRequest);
    }

}