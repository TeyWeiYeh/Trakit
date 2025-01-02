package mdad.localdata.trakit.mainfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
import java.util.List;
import java.util.Map;

import data.network.ICallback;
import data.network.VolleySingleton;
import data.network.controller.CategoryController;
import domain.Category;
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
    String type = Category.Type.EXPENSE.toString();
    EditText etCat;
    TextView cid;
    Button btnSave, btnDelete;
    //Button btnEditCat;
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
                    sharedPreferences.edit().putString("token",null).apply();
                    Intent i = new Intent(getContext(), AuthActivity.class);
                    //clears the stack
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        catList(type);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.expenseButton) {
                expenseButton.setBackgroundResource(R.drawable.rounded_left_selected);
                incomeButton.setBackgroundResource(R.drawable.rounded_right_unselected);
                type = Category.Type.EXPENSE.toString();
                System.out.println("Token" + token);
            } else if (checkedId == R.id.incomeButton) {
                incomeButton.setBackgroundResource(R.drawable.rounded_right_selected);
                expenseButton.setBackgroundResource(R.drawable.rounded_left_unselected);
                type = Category.Type.INCOME.toString();
                System.out.println("Token" + token);
            }
            catList(type);
        });
    }

    public void catList (String type){
        CategoryController categoryController = new CategoryController(getContext());
        ArrayList<HashMap<String,String>> arrayList=new ArrayList<>();
        categoryController.getAllCategories(type, new ICallback(){
            @Override
            public void onSuccess(Object result) {
                JSONArray dataArray = (JSONArray) result;
                try{
                    for (int i=0; i<dataArray.length();i++){
                        JSONObject item = dataArray.getJSONObject(i);
                        String id = item.getString("id");
                        String name = item.getString("name");
                        String type = item.getString("type");
                        HashMap<String,String> hashMap=new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("name",name);
                        hashMap.put("type",type);
                        arrayList.add(hashMap);
                    }
                    String[] from={"id","name","type"};//string array
                    int[] to={R.id.catId, R.id.tvName,R.id.tvType};//int array of views id's
                    SimpleAdapter simpleAdapter=new SimpleAdapter(getContext(),arrayList,R.layout.list_view_items,from,to){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            Button btnEditCat = view.findViewById(R.id.btnEditCat);
                            btnEditCat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Create and show the popup window
                                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View popupView = inflater.inflate(R.layout.popup, null);
                                    etCat = popupView.findViewById(R.id.etCat);
                                    btnSave = popupView.findViewById(R.id.btnSave);
                                    btnDelete = popupView.findViewById(R.id.btnDelete);
                                    String catId = ((TextView) view.findViewById(R.id.catId)).getText().toString();
                                    Category.Type catType = ((TextView) view.findViewById(R.id.tvType)).getText().toString().equals("Expense") ? Category.Type.EXPENSE : Category.Type.INCOME;
                                    cid = popupView.findViewById(R.id.cid);
                                    cid.setText(catId);
                                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    boolean focusable = true;
                                    PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                                    popupWindow.showAtLocation(v, Gravity.CENTER, 50, 50);
                                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                                    popupWindow.setElevation(10); // Add shadow if supported
                                    dimBehind(popupWindow);
                                    btnSave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Category newCat = new Category(cid.getText().toString(), etCat.getText().toString(),catType);
                                            System.out.println("Update Cat: " + newCat.id + " " + newCat.name + " " + catType);
                                            categoryController.updateCategory(catId, newCat, new ICallback(){
                                                @Override
                                                public void onSuccess(Object result) {
                                                    popupWindow.dismiss();
                                                    catList(type);
                                                    Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                                                }
                                                @Override
                                                public void onError(String error) {
                                                    Toast.makeText(getContext(), "Error updating category: " + error, Toast.LENGTH_LONG).show();
                                                }
                                                @Override
                                                public void onAuthFailure(String authFailure){
                                                    Toast.makeText(getContext(), authFailure, Toast.LENGTH_LONG).show();
                                                    // Redirect the user to the login screen
                                                    Intent intent = new Intent(getContext(), AuthActivity.class);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                    btnDelete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            categoryController.deleteCategory(cid.getText().toString(), new ICallback(){
                                                @Override
                                                public void onSuccess(Object result){
                                                    popupWindow.dismiss();
                                                    catList(type);
                                                    Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                                                }
                                                @Override
                                                public void onError(String error){
                                                    Toast.makeText(getContext(), "Error updating category: " + error, Toast.LENGTH_LONG).show();
                                                }
                                                @Override
                                                public void onAuthFailure(String authFailure){
                                                    Toast.makeText(getContext(), authFailure, Toast.LENGTH_LONG).show();
                                                    // Redirect the user to the login screen
                                                    Intent intent = new Intent(getContext(), AuthActivity.class);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            return view;
                        }
                    };
                    category_list.setAdapter(simpleAdapter);
                } catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Json Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onError(String error) {
                // Handle error
                Toast.makeText(getContext(), "API error: " + error, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAuthFailure(String authFailure){
                Toast.makeText(getContext(), authFailure, Toast.LENGTH_LONG).show();
                // Redirect the user to the login screen
                Intent intent = new Intent(getContext(), AuthActivity.class);
                startActivity(intent);
            }
        });
    }
    private void dimBehind(PopupWindow popupWindow) {
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.5f; // Adjust dim amount (0.0 - no dim, 1.0 - fully dimmed)
        wm.updateViewLayout(container, p);
    }
}