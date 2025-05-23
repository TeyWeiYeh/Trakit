package mdad.localdata.trakit.authfragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import data.network.VolleySingleton;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.MainActivity;
import mdad.localdata.trakit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignupFragment newInstance(String param1, String param2) {
        SignupFragment fragment = new SignupFragment();
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
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    EditText etSignupEmail, etSignupUsername, etSignupPassword, etSignupSecondaryPassword;
    Button btnSignup;
    TextView tvToLogin;
    String email, username, password, confpassword;
    private static String url_signup = MainActivity.ipBaseUrl + "/signup.php";
    public void onViewCreated(View view, Bundle savedInstanceState){
        TextInputLayout passwordLayout = view.findViewById(R.id.filledTextFieldCat);
        TextInputLayout confPasswordLayout = view.findViewById(R.id.filledTextFieldSecondaryPassword);
        etSignupEmail = (EditText) view.findViewById(R.id.etSignupEmail);
        etSignupUsername = (EditText) view.findViewById(R.id.etSignupUsername);
        etSignupPassword = (EditText) view.findViewById(R.id.etSignupPassword);
        etSignupSecondaryPassword = (EditText) view.findViewById(R.id.etSignupConfirmPassword);
        btnSignup = (Button) view.findViewById(R.id.btnSignup);
        tvToLogin = (TextView) view.findViewById(R.id.tvToLogin);
        password = etSignupPassword.getText().toString();
        confpassword = etSignupSecondaryPassword.getText().toString();

        //ensures the user password and confirm password matches before proceeding
        etSignupSecondaryPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean result = matchPassword(etSignupPassword.getText().toString(), etSignupSecondaryPassword.getText().toString());
                if (result) {
                    confPasswordLayout.setError(null);
                } else {
                    confPasswordLayout.setError(getString(R.string.error_password_not_match));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        etSignupPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean result = matchPassword(password, confpassword);
                if (result){
                    passwordLayout.setError(null);
                }
                else{
                    passwordLayout.setError(getString(R.string.error_password_not_match));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trimEmail = etSignupEmail.getText().toString().trim();
                String trimUsername = etSignupUsername.getText().toString().trim();
                String trimPassword = etSignupPassword.getText().toString().trim();
                String confirmPassword = etSignupSecondaryPassword.getText().toString().trim();
                //ensure all fields are filled in and passwords are the same
                if (!trimEmail.isEmpty() && !trimUsername.isEmpty() && !trimPassword.isEmpty()){
                    if (!trimPassword.equals(confirmPassword)){
                        Toast.makeText(getContext(), "Passwords don't match", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Map<String, String> params_signup = new HashMap<String, String>();
                        params_signup.put("email", trimEmail);
                        params_signup.put("username", trimUsername);
                        params_signup.put("password", trimPassword);
                        Signup(url_signup, params_signup);
                    }
                } else {
                    Toast.makeText(requireContext().getApplicationContext(), "Please fill up all fields", Toast.LENGTH_LONG).show();
                }
            }
        });
        //navigate to the login page
        tvToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager(); // Use getActivity().getSupportFragmentManager() if not in parent-child hierarchy
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, new LoginFragment());
                transaction.addToBackStack(null); // Add to backstack to enable "Back" navigation
                transaction.commit();
            }
        });
    }

    private boolean matchPassword(String password, String confPassword){
        return password.equals(confPassword);
    }

    //function to sign up the user, if successful then token will be stored in shared preferences
    public void Signup(String url, Map params ){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    String token = jsonObject.getString("token");
                    Log.d("token", token);
                    if (message.equals("User successfully created")) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        storeToken(token);
                        Intent goToMainActivity = new Intent(getContext(), MainActivity.class);
                        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(goToMainActivity);
                    } else if (message.equals("User already exists")){
                        Toast.makeText(getActivity(), "User already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Response error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    String errorMessage = new String(error.networkResponse.data);

                    if (statusCode == 409) {
                        // Handle "User already exists" error
                        Toast.makeText(getActivity(), "User already exists. Please try logging in.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Error: " + statusCode + " - " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Network error (no response)
                    Toast.makeText(getActivity(), "Network error: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Nullable
            @Override
            // to send product info stored in HashMap params_create to server via HTTP Post
            protected Map<String, String> getParams() {
                return params;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
    public void storeToken(String token) {
        AuthActivity.getSharedPreferences().edit().putString("token", token).apply();
    }
}