package mdad.localdata.trakit.mainfragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
import data.network.controller.TransactionController;
import domain.Category;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.MainActivity;
import mdad.localdata.trakit.ProfileActivity;
import mdad.localdata.trakit.R;
import mdad.localdata.trakit.authfragments.LoginFragment;
import mdad.localdata.trakit.transactionfragments.AllTransactionsFragment;

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
    String token, id, name, userId, transactionCount;
    ListView category_list;
    String type = Category.Type.EXPENSE.toString();
    EditText etCat, etCreateCat;
    TextView cid, tvUserId;
    Button btnSave, btnDelete, btnOpenAddPopup, btnAdd, btnCancel;
    RadioGroup radioGroup;
    RadioButton expenseButton, incomeButton;
    CategoryController categoryController;
    SharedPreferences sharedPreferences;
    TransactionController transactionController;
    Integer intTransCount;
    //Button btnEditCat;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        categoryController = new CategoryController(getContext());
        transactionController = new TransactionController(getContext());
        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        topAppBar = view.findViewById(R.id.topAppBar);
        FragmentManager fm = getParentFragmentManager();
        btnOpenAddPopup = (Button) view.findViewById(R.id.add);
        //implement logout and navigate to profile activity
        topAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_profile) {
                    Intent goToProfilePage = new Intent(getContext(), ProfileActivity.class);
                    startActivity(goToProfilePage);
                    return true;
                } else if (itemId == R.id.action_logout) {
                    sharedPreferences.edit().putString("token",null).apply();
                    Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                    goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(goToLoginPage);
                    return true;
                } else
                    return false;
            }
        });
        radioGroup = view.findViewById(R.id.radioGroup);
        expenseButton = view.findViewById(R.id.expenseButton);
        incomeButton = view.findViewById(R.id.incomeButton);

        category_list = view.findViewById(R.id.category_list);
        radioGroup.check(R.id.expenseButton);
        catList(type);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.expenseButton) {
                expenseButton.setBackgroundResource(R.drawable.rounded_left_selected);
                incomeButton.setBackgroundResource(R.drawable.rounded_right_unselected);
                type = Category.Type.EXPENSE.toString();
            } else if (checkedId == R.id.incomeButton) {
                incomeButton.setBackgroundResource(R.drawable.rounded_right_selected);
                expenseButton.setBackgroundResource(R.drawable.rounded_left_unselected);
                type = Category.Type.INCOME.toString();
            }
            catList(type);
        });
        btnOpenAddPopup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View createPopup = inflater.inflate(R.layout.create_category_popup, null);
                btnAdd = (Button) createPopup.findViewById(R.id.btnSave_create);
                btnCancel = (Button) createPopup.findViewById(R.id.btnCancel);
                etCreateCat = (EditText) createPopup.findViewById(R.id.etCreateCat);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                PopupWindow createPopupWindow = new PopupWindow(createPopup, width, height, focusable);
                createPopupWindow.showAtLocation(view, Gravity.CENTER, 50, 50);
                createPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                createPopupWindow.setElevation(10);
                createPopupWindow.setFocusable(true);
                dimBehind(createPopupWindow);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createPopupWindow.dismiss();
                    }
                });

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Category.Type catType = expenseButton.getText().toString().equals("Expense") ? Category.Type.EXPENSE : Category.Type.INCOME;
                        Category.Type catType = radioGroup.getCheckedRadioButtonId() == R.id.expenseButton
                                ? Category.Type.EXPENSE : Category.Type.INCOME;
                        Category newCat = new Category(etCreateCat.getText().toString(), catType);
                        System.out.println("Create cat name: " + newCat.name + " Cat type: " + catType);
                        categoryController.createCategory(newCat, new ICallback() {
                            @Override
                            public void onSuccess(Object result) {
                                createPopupWindow.dismiss();
                                catList(catType.toString());
                                Toast.makeText(requireContext(), "Created Category Successfully", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed to create category", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onAuthFailure(String message) {
                                Intent i = new Intent(getContext(), AuthActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        });
                    }
                });
            }
        });
    }

    public void catList(String type) {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        categoryController.getAllCategories(type, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null) {
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONArray dataArray = (JSONArray) result;
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        id = item.getString("id");
                        name = item.getString("name");
                        if (name.length() > 10) {
                            name = name.substring(0, 5) + "...";
                        }
                        String type = item.getString("type");
                        userId = item.getString("userId");
                        transactionCount = item.getString("transaction_count");
                        intTransCount = Integer.parseInt(transactionCount);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("name", name);
                        hashMap.put("type", type);
                        hashMap.put("userId", userId);
                        hashMap.put("trans_count", transactionCount);
                        arrayList.add(hashMap);
                    }

                    String[] from = {"id", "name", "trans_count", "userId"}; // string array
                    int[] to = {R.id.catId, R.id.tvName, R.id.tvNumRecord, R.id.tvUserId}; // int array of views id's
                    SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), arrayList, R.layout.list_view_items, from, to) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            Button btnEditCat = view.findViewById(R.id.btnEditCat);
                            TextView tvNumRecord = view.findViewById(R.id.tvNumRecord);
                            HashMap<String, String> item = (HashMap<String, String>) arrayList.get(position);
                            String popupUserId = item.get("userId");
                            String popupTransCount = item.get("trans_count");
                            Integer intPopupTransCount = Integer.parseInt(popupTransCount);
                            if (intPopupTransCount == 0){
                                tvNumRecord.setText("No");
                            } else if (popupUserId.equals("null")){
                                btnEditCat.setVisibility(View.INVISIBLE);
                            }
                            else{
                                btnEditCat.setVisibility(View.VISIBLE);
                                tvNumRecord.setText(popupTransCount);
                            }
                            btnEditCat.setOnClickListener(v -> {
                                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View popupView = inflater.inflate(R.layout.popup, null);
                                etCat = popupView.findViewById(R.id.etCat);
                                btnSave = popupView.findViewById(R.id.btnSave);
                                btnDelete = popupView.findViewById(R.id.btnDelete);
                                tvUserId = popupView.findViewById(R.id.tvUserId);
//                                String popupUserId = tvUserId.getText().toString();
                                String catId = ((TextView) view.findViewById(R.id.catId)).getText().toString();
                                Category.Type catType = expenseButton.getText().toString().equals("Expense") ? Category.Type.EXPENSE : Category.Type.INCOME;
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
                                btnSave.setOnClickListener(view1 -> {
                                    Category updateCat = new Category(cid.getText().toString(), etCat.getText().toString(), catType);
                                    categoryController.updateCategory(catId, updateCat, new ICallback() {
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
                                        public void onAuthFailure(String authFailure) {
                                            Intent intent = new Intent(getContext(), AuthActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    });
                                });
                                if (intPopupTransCount > 0 || popupUserId.equals("null")){
                                    btnDelete.setEnabled(false);
                                    Toast.makeText(getContext(), "Category cannot be deleted", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    btnDelete.setEnabled(true);
                                    btnDelete.setOnClickListener(view1 -> {
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("Delete")
                                                .setMessage("Are you sure?")
                                                .setNegativeButton(getResources().getString(R.string.btnCancel), (dialog, which) -> {
                                                    dialog.dismiss();
                                                })
                                                .setPositiveButton(getResources().getString(R.string.btnConfirm), (dialog, which) -> {
                                                    categoryController.deleteCategory(cid.getText().toString(), new ICallback() {
                                                        @Override
                                                        public void onSuccess(Object result) {
                                                            popupWindow.dismiss();
                                                            catList(type);
                                                            Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                                                        }

                                                        @Override
                                                        public void onError(String error) {
                                                            Toast.makeText(getContext(), "Error deleting category: " + error, Toast.LENGTH_LONG).show();
                                                        }

                                                        @Override
                                                        public void onAuthFailure(String authFailure) {
                                                            Toast.makeText(getContext(), authFailure, Toast.LENGTH_LONG).show();
                                                            // Redirect the user to the login screen
                                                            Intent intent = new Intent(getContext(), AuthActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                })
                                                .setIcon(ContextCompat.getDrawable(getContext(), R.drawable.warning_icon))
                                                .show();
                                    });
                                }
                            });
                            return view;
                        }
                    };
                    category_list.setAdapter(simpleAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error parsing category data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error fetching categories: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent i = new Intent(getContext(), AuthActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
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

    private void getAllTransactionsByCatId(String id){
        transactionController.getTransactionsByCatId(id, new ICallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onAuthFailure(String message) {

            }
        });
    }
}