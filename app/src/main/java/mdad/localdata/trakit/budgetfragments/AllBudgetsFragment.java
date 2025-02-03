package mdad.localdata.trakit.budgetfragments;

import static utils.DateUtils.getShortMonth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;

import adapters.BudgetAdapter;
import data.network.ApiController;
import data.network.ICallback;
import data.network.controller.BudgetController;
import data.network.controller.TransactionController;
import data.network.controller.WalletController;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.ProfileActivity;
import mdad.localdata.trakit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllBudgetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllBudgetsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllBudgetsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllBudgetsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllBudgetsFragment newInstance(String param1, String param2) {
        AllBudgetsFragment fragment = new AllBudgetsFragment();
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
        return inflater.inflate(R.layout.fragment_all_budgets, container, false);
    }

    WalletController walletController;
    String token, selectedMonthYear, income, expense, balance, budgetId, budgetName, budgetSD, budgetED, budgetLimit, budgetUID, budgetExp, budgetInc, budgetBal;
    Button btnExportCSV, btnReadCSV, btnAddBudget;
    ImageButton btnPrevMonth, btnNextMonth;
    TextView tvIncome, tvExpense, tvBalance, tvMonth, tvBalanceMinusIcon;
    RecyclerView rvAllBudgets;
    BudgetController budgetController;
    MaterialToolbar topAppBar;
    SharedPreferences sharedPreferences;
    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    int currentYear = Year.now().getValue();
    int currentIndex = 0;

    public void onViewCreated(View view, Bundle savedInstanceState){
        walletController = new WalletController(getContext());
        budgetController = new BudgetController(getContext());
        topAppBar = view.findViewById(R.id.topAppBar);
        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        btnExportCSV = view.findViewById(R.id.btnExportCSV);
        btnReadCSV = view.findViewById(R.id.btnReadCSV);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvBalanceMinusIcon = view.findViewById(R.id.tvBalanceMinusIcon);
        rvAllBudgets = view.findViewById(R.id.rvAllBudgets);
        getAllBudgets();
        tvMonth = view.findViewById(R.id.tvMonth);
        tvMonth.setText(getShortMonth(months[currentIndex]));
        selectedMonthYear = months[currentIndex] + " " + currentYear;
        getWallet();

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

        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex > 0) {
                    currentIndex--; // Move to previous month
                } else {
                    currentIndex = months.length - 1; // Loop back to December
                }
                tvMonth.setText(getShortMonth(months[currentIndex]));
                selectedMonthYear = months[currentIndex] + " " + currentYear;
                getWallet();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex < months.length - 1) {
                    currentIndex++;
                } else {
                    currentIndex = 0;
                }
                tvMonth.setText(getShortMonth(months[currentIndex]));
                selectedMonthYear = months[currentIndex] + " " + currentYear;
                getWallet();
            }
        });

        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createBudgetFragment = new CreateNewBudgetFragment();
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.home_fragment, createBudgetFragment).commit();
            }
        });
    }

    public void getWallet(){
        walletController.getWalletData(selectedMonthYear, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                try{
                    JSONObject responseObject = (JSONObject) result;
                    income = responseObject.getString("income");
                    expense = responseObject.getString("expense");
                    balance = responseObject.getString("balance");
                    Float floatBalance =Float.parseFloat((balance));
                    if (floatBalance < 0) {
                        floatBalance = Math.abs(floatBalance);
                        tvBalanceMinusIcon.setVisibility(View.VISIBLE);
                    } else {
                        tvBalanceMinusIcon.setVisibility(View.GONE); // Hide the minus icon when balance is 0 or positive
                    }
                    DecimalFormat df = new DecimalFormat("0.00");
                    df.setMaximumFractionDigits(2);
                    balance = df.format(floatBalance);
                    tvIncome.setText(income);
                    tvExpense.setText(expense);
                    tvBalance.setText(balance);
                } catch (Exception e){
                    Log.d("Error", e.getMessage());
                    Toast.makeText(getContext(), "Error "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }

    public void getAllBudgets(){
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        budgetController.getAllBudgets(new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null){
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    JSONArray transListArray = (JSONArray) result;
                    for (int i=0; i<transListArray.length(); i++){
                        JSONObject budget = transListArray.getJSONObject(i);
//                        budgetId = budget.getString("id");
//                        budgetName = budget.getString("name");
//                        budgetSD = budget.getString("start_date");
//                        budgetED = budget.getString("end_date");
//                        budgetLimit = budget.getString("limit");
//                        budgetExp = budget.getString("total_spent");
//                        budgetInc = budget.getString("total_saved");
//                        budgetBal = budget.getString("balance");
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", budget.getString("id"));
                        hashMap.put("name", budget.getString("name"));
                        hashMap.put("start_date", budget.getString("start_date"));
                        hashMap.put("end_date", budget.getString("end_date"));
                        hashMap.put("limit", budget.getString("limit"));
                        hashMap.put("total_spent", budget.getString("total_spent"));
                        hashMap.put("total_saved", budget.getString("total_saved"));
                        hashMap.put("balance", budget.getString("balance"));
                        arrayList.add(hashMap);
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    rvAllBudgets.setLayoutManager(layoutManager);
                    rvAllBudgets.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    rvAllBudgets.setAdapter(new BudgetAdapter(getContext(), arrayList, getParentFragmentManager()));
                } catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }
}