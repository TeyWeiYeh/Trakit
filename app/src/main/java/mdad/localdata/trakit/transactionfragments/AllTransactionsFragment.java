package mdad.localdata.trakit.transactionfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import adapters.TransactionAdapter;
import data.network.ICallback;
import data.network.controller.TransactionController;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.ProfileActivity;
import mdad.localdata.trakit.R;
import utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllTransactionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTransactionsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllTransactionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllTransactionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllTransactionsFragment newInstance(String param1, String param2) {
        AllTransactionsFragment fragment = new AllTransactionsFragment();
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
        return inflater.inflate(R.layout.fragment_all_transactions, container, false);
    }

    String token, transId, transAmount, transDescription, transDateCreated, transDateUpdated, transRecurring, transBudgetId, transUserId, transImage, transCatId, transCatName, transCatType, transDate;
    MaterialToolbar transTopAppBar;
    TransactionController transactionController;
    ListView transListView;
    Button btnChooseDate;
    Spinner spinnerMonthYear;
    int currentYear, currentMonth;
    String monthName, selectedMonthYear;
    Fragment currentFragment;

    public void onViewCreated(View view, Bundle savedInstanceState){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        transTopAppBar = (MaterialToolbar) view.findViewById(R.id.topAppBar);
        transListView = (ListView) view.findViewById(R.id.transListView);
        spinnerMonthYear = (Spinner) view.findViewById(R.id.spinnerMonthYear);
        transactionController = new TransactionController(getContext());
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        monthName = new DateFormatSymbols().getMonths()[currentMonth];
        int fragmentId = this.getId();
//        currentFragment = requireActivity().getSupportFragmentManager().getFragment()
        transTopAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_profile) {
                    Intent goToProfilePage = new Intent(getContext(), ProfileActivity.class);
                    goToProfilePage.putExtra("currentPage", fragmentId);
                    startActivity(goToProfilePage);
                    return true;
                }
                else if (itemId == R.id.action_logout){
                    sharedPreferences.edit().putString("token",null).apply();
                    Intent i = new Intent(getContext(), AuthActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return true;
                }
                else
                    return false;
            }
        });

        List<String> monthYearList = new ArrayList<>();
        for (int year = currentYear; year >= currentYear - 3; year--) {
            int monthLimit = (year == currentYear) ? currentMonth + 1 : 12;

            for (int month = monthLimit - 1; month >= 0; month--) {
                monthName = new DateFormatSymbols().getMonths()[month];
                monthYearList.add(monthName + " " + year);
            }
        }
        selectedMonthYear = monthYearList.get(0);
        transList();

        // Set up adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monthYearList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthYear.setAdapter(adapter);

        spinnerMonthYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthYear = parent.getItemAtPosition(position).toString();
                transList();
                // Handle the selected month and year
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection if needed
            }
        });
    }

    public void transList(){
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        transactionController.getAllTransactions(selectedMonthYear, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null){
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    JSONArray transListArray = (JSONArray) result;
                    for (int i=0; i<transListArray.length(); i++){
                        JSONObject transaction = transListArray.getJSONObject(i);
                        transId = transaction.getString("transactionId");
                        transAmount = transaction.getString("amount");
                        transDescription = transaction.getString("description");
                        transDateCreated = transaction.getString("date_created");
                        transDateUpdated = transaction.getString("date_updated");
                        transDate = transaction.getString("trans_date");
                        transRecurring = transaction.getString("recurring");
                        transBudgetId = transaction.getString("budgetId");
                        transUserId = transaction.getString("userId");
                        transImage = transaction.getString("image");
                        transCatId = transaction.getString("categoryId");
                        transCatName = transaction.getString("categoryName");
                        transCatType = transaction.getString("categoryType");
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("type", StringUtils.capitalizeFirstLetter(transCatType));
                        hashMap.put("date", StringUtils.convertDateFormat(transDate));
                        hashMap.put("catName", StringUtils.capitalizeFirstLetter(transCatName));
                        hashMap.put("amount", transAmount);
                        hashMap.put("typeIcon", "-");
                        hashMap.put("desc", transDescription);
                        hashMap.put("image", transImage);
                        hashMap.put("id", transId);
                        hashMap.put("recurring", transRecurring);
                        arrayList.add(hashMap);
                    }
                    TransactionAdapter adapter = new TransactionAdapter(getContext(), arrayList, getParentFragmentManager());
                    transListView.setAdapter(adapter);
                } catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error parsing category data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to retrieve data: " + error, Toast.LENGTH_LONG).show();
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