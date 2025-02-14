package mdad.localdata.trakit.budgetfragments;

import static androidx.core.content.ContextCompat.getSystemService;
import static utils.DateUtils.getShortMonth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;

import adapters.BudgetAdapter;
import data.network.ApiController;
import data.network.ICallback;
import data.network.controller.BudgetController;
import data.network.controller.MonthlyReportController;
import data.network.controller.TransactionController;
import data.network.controller.WalletController;
import domain.MonthlyReport;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.ProfileActivity;
import mdad.localdata.trakit.R;
import utils.FileUtils;
import utils.StringUtils;

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
    MonthlyReportController monthlyReportController;
    String token, selectedMonthYear, income, expense, balance;
    Button btnAddBudget;
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
    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1;

    int currentYear = Year.now().getValue();
    int currentIndex = month -1;
    String monthYearSql = year + "-" + String.format("%02d", month);
    HSSFWorkbook workbook;
    ListView lvMonthlyStatement;
    String workbookName = monthYearSql +"-"+ "report";
    private static final int PERMISSION_REQUEST_CODE = 100;

    public void onViewCreated(View view, Bundle savedInstanceState){
        walletController = new WalletController(getContext());
        budgetController = new BudgetController(getContext());
        monthlyReportController = new MonthlyReportController(getContext());
        topAppBar = view.findViewById(R.id.topAppBar);
        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvBalanceMinusIcon = view.findViewById(R.id.tvBalanceMinusIcon);
        rvAllBudgets = view.findViewById(R.id.rvAllBudgets);
        lvMonthlyStatement = view.findViewById(R.id.lvMonthlyStatement);
        getAllBudgets();
        tvMonth = view.findViewById(R.id.tvMonth);
        tvMonth.setText(getShortMonth(months[currentIndex]));
        selectedMonthYear = months[currentIndex] + " " + currentYear;
        getWallet();
        monthlyStatementList();

        //actions like logout and go to profile from the top nav bar
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
        //button to go to previous month
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

        //button to go to next month
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

        //go to create budget page
        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment createBudgetFragment = new CreateNewBudgetFragment();
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.home_fragment, createBudgetFragment).commit();
            }
        });
    }

    //get the wallet data (spending, income, balance) for the logged in user in the landing page
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

    //get all the budgets of the user and populate the recycler view custom adapter
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
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", budget.getString("id"));
                        hashMap.put("name", StringUtils.trimString(budget.getString("name")));
                        hashMap.put("fullName", budget.getString("name"));
                        hashMap.put("start_date", budget.getString("start_date"));
                        hashMap.put("end_date", budget.getString("end_date"));
                        hashMap.put("limit", budget.getString("limit"));
                        hashMap.put("total_spent", budget.getString("total_spent"));
                        hashMap.put("total_saved", budget.getString("total_saved"));
                        hashMap.put("balance", budget.getString("budget_balance"));
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

    //store the excel file in the downloads folder
    public void storeWorkBook(HSSFWorkbook hssfWorkbook) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API level 29) and above - Scoped Storage using MediaStore
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, hssfWorkbook.getSheetName(0) + ".xlsx");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                // Get Uri to insert the file into Downloads folder
                Uri uri = getActivity().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        hssfWorkbook.write(outputStream);
                        hssfWorkbook.close();
                        outputStream.close();
                        Toast.makeText(getContext(), "Excel Downloaded", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error exporting Excel", Toast.LENGTH_SHORT).show();
            }
        } else {
            // For Android versions below 10 (API level 29) - Legacy Storage
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), workbookName + ".xlsx");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                hssfWorkbook.write(fileOutputStream);
                hssfWorkbook.close();
                fileOutputStream.close();
                Toast.makeText(getContext(), "Excel Downloaded", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error exporting Excel", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //display all the monthly reports generated for the user in a list view
    public void monthlyStatementList(){
        monthlyReportController.getAllReports(new ICallback() {
            ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            @Override
            public void onSuccess(Object result) {
                if (result == null) {
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONArray dataArray = (JSONArray) result;
                    for (int i=0; i<dataArray.length(); i++){
                        JSONObject item = dataArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", item.getString("id"));
                        hashMap.put("name", item.getString("name"));
                        hashMap.put("file", item.getString("file"));
                        hashMap.put("month", item.getString("month"));
                        arrayList.add(hashMap);
                    }

                    String[] from = {"name", "file"};
                    int[] to = {R.id.tvName, R.id.tvFile};

                    SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), arrayList, R.layout.statement_list_view, from, to){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            Button btnDownload = view.findViewById(R.id.btnDownload);
                            HashMap<String, String> item = arrayList.get(position);
                            String base64File = item.get("file");

                            btnDownload.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    HSSFWorkbook workbook = FileUtils.decodeBase64ToWorkbook(base64File);
                                    storeWorkBook(workbook);  // Use the storeWorkBook method here
                                }
                            });
                            return view;
                        }
                    };

                    lvMonthlyStatement.setAdapter(simpleAdapter);
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error fetching monthly report", Toast.LENGTH_LONG).show();
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


    //handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storeWorkBook(workbook);
            } else {
                // Permission denied, inform the user
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}