package mdad.localdata.trakit.mainfragments;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import data.network.ApiController;
import data.network.ICallback;
import data.network.controller.TransactionController;
import helpers.NotificationHelper;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.R;
import mdad.localdata.trakit.budgetfragments.AllBudgetsFragment;
import mdad.localdata.trakit.budgetfragments.CreateNewBudgetFragment;
import workers.RecurringBillWorker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    Button btnExportCSV, btnReadCSV, btnAddBudget;
    EditText etQuery;
    TextView tvResponse, tvIncome, tvExpense, tvExpenseIcon, tvBalance;
    private final String url = "https://api.deepseek.com/chat/completions";
    ArrayList<Map<String,String>> chat;
    JSONArray contentArray = new JSONArray();
    ApiController apiController;
    TransactionController transactionController;
    String transDate, transCategory, transAmount;
    public void onViewCreated(View view, Bundle savedInstanceState){
        transactionController = new TransactionController(getContext());
//        btnExportCSV = view.findViewById(R.id.btnExportCSV);
//        btnReadCSV = view.findViewById(R.id.btnReadCSV);
//        btnAddBudget = view.findViewById(R.id.btnAddBudget);
//        tvIncome = view.findViewById(R.id.tvIncome);
//        tvExpense = view.findViewById(R.id.tvExpense);
//        tvBalance = view.findViewById(R.id.tvBalance);
//        btnAddBudget.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Fragment createBudgetFragment = new CreateNewBudgetFragment();
//                FragmentManager fm = getChildFragmentManager();
//                fm.beginTransaction().replace(getContext(), createBudgetFragment).commit();
//            }
//        });
//        btnExportCSV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                }
//                printMonthlyStatement();
//            }
//        });
//        btnReadCSV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                readDataLineByLine("jan_report.csv");
//            }
//        });
        requestNotificationPermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        PeriodicWorkRequest fetchRecurringBills = new PeriodicWorkRequest.Builder(
                RecurringBillWorker.class,
                1, TimeUnit.DAYS,  // Repeat every 1 day
                1, TimeUnit.HOURS  // Allow 1-hour flex time
        ).build();
        WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
                "RecurringBillWorker", // Unique name for the worker
                ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
                fetchRecurringBills
        );
        Fragment allBudgetFragment = new AllBudgetsFragment();
                FragmentManager fm = getChildFragmentManager();
                fm.beginTransaction().replace(R.id.home_fragment, allBudgetFragment).commit();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
                NotificationHelper.createNotificationChannel(requireContext());
            } else {
                Toast.makeText(getContext(), "Notification permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportcsv(){
        Date currentTime = Calendar.getInstance().getTime();
        String fileName = "jan_report.csv";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try (FileWriter fileWriter = new FileWriter(file);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // Sample Data - Replace with actual data
            String[] header = {"Date", "Category", "Amount"};
            String[] row1 = {"1", "Alice", "24"};
            String[] row2 = {"2", "Bob", "30"};

            csvWriter.writeNext(header);
            csvWriter.writeNext(row1);
            csvWriter.writeNext(row2);

            Toast.makeText(getContext(), "CSV Exported: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    public static void readDataLineByLine(String fileName){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try (CSVReader csvReader = new CSVReader(new FileReader(file))) {
            List<String[]> data = csvReader.readAll();

            StringBuilder csvContent = new StringBuilder();
            for (String[] row : data) {
                csvContent.append(Arrays.toString(row)).append("\n");
            }
            Log.d("csv content", String.valueOf(csvContent));

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void getWalletData(String monthYear){

    }

    public void printMonthlyStatement(){
        Calendar calendar = Calendar.getInstance();

        // Define the format: "MMMM yyyy" (Month name and year)
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH);

        // Format the current date
        String formattedDate = sdf.format(calendar.getTime());
        transactionController.getAllTransactions(formattedDate, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null){
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    JSONArray transListArray = (JSONArray) result;
                    String[] headers = {"Date", "Category", "Amount"};
                    String[] date = new String[transListArray.length()];
                    String[] cat = new String[transListArray.length()];
                    String[] amt = new String[transListArray.length()];
                    for (int i=0; i<transListArray.length(); i++) {
                        JSONObject transaction = transListArray.getJSONObject(i);
                        transDate = transaction.getString("trans_date");
                        transCategory = transaction.getString("categoryName");
                        transAmount = transaction.getString("amount");
                        date[i] = transDate;
                        cat[i] = transCategory;
                        amt[i] = transAmount;
                    }
                    String fileName = "jan_report.csv";
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                    FileWriter fileWriter = new FileWriter(file);
                    CSVWriter csvWriter = new CSVWriter(fileWriter);
                    csvWriter.writeNext(headers);
                    csvWriter.writeNext(cat);
                    csvWriter.writeNext(amt);
                    fileWriter.close();

                    Toast.makeText(getContext(), "CSV Exported: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
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


//    private void getResponse(String query){
//
//        RequestQueue requestQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();
//        JSONObject jsonObject = new JSONObject();
//        try {
//            JSONObject userQuery = new JSONObject();
//            userQuery.put("role", "user");
//            userQuery.put("content", query);
//            contentArray.put(userQuery);
//            // Adding params to JSON object
//            jsonObject.put("model", "deepseek-chat");
//            jsonObject.put("messages", contentArray);
//            jsonObject.put("temperature", 1.3);
//            jsonObject.put("max_tokens", 20);
//            Log.d("json", String.valueOf(contentArray));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    String responseMsg = response
//                            .getJSONArray("choices")
//                            .getJSONObject(0)
//                            .getJSONObject("message")
//                            .getString("content");
//                    String role = response
//                            .getJSONArray("choices")
//                            .getJSONObject(0)
//                            .getJSONObject("message")
//                            .getString("role");
//                    JSONObject respObj = new JSONObject();
//                    respObj.put("role", role);
//                    respObj.put("content", responseMsg);
//                    Log.d("Response", responseMsg);
//                    contentArray.put(respObj);
//                    Log.d("chat", String.valueOf(contentArray));
//                    tvResponse.setText(responseMsg);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("TAGAPI", "Error is : " + error.getMessage() + "\n" + error);
//            }
//        }){
//            public Map<String, String> getHeaders() {
//                Map<String, String> params = new HashMap<>();
//                // Adding headers
//                params.put("Content-Type", "application/json");
//                params.put("Accept", "application/json");
//                params.put("Authorization", "Bearer sk-e0760c05d9c34feb9328b07be245d500");
//                return params;
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//    }
}