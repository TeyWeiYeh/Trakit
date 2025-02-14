package workers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import data.network.ICallback;
import data.network.controller.BudgetController;
import data.network.controller.MonthlyReportController;
import data.network.controller.TransactionController;
import data.network.controller.WalletController;
import domain.MonthlyReport;
import domain.Transaction;
import helpers.NotificationHelper;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.R;
import mdad.localdata.trakit.budgetfragments.AllBudgetsFragment;
import utils.StringUtils;

public class RecurringBillWorker extends Worker {

    private final TransactionController transactionController;
    private final String formattedDate;
    private final String formattedCurrDate;
    private int totalTransactions;
    private int processedTransactions;
    private JSONArray transListArray;
    String token, selectedMonthYear, income, expense, balance;
    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1;
    int today = c.get(Calendar.DAY_OF_MONTH);
    int lastDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);

    String monthYearSql = year + "-" + String.format("%02d", month);
    HSSFWorkbook workbook;
    WalletController walletController;
    String workbookName = monthYearSql +"-"+ "report";
    MonthlyReportController monthlyReportController;
    JSONArray dataResponse;
    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    SharedPreferences sharedPreferences;
    MonthlyReport monthlyReport;
    BudgetController budgetController;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public RecurringBillWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        transactionController = new TransactionController(context);
        walletController = new WalletController(context);
        monthlyReportController = new MonthlyReportController(context);
        Date currDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
        formattedDate = df.format(currDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        formattedCurrDate = sdf.format(currDate);
        selectedMonthYear = months[c.get(Calendar.MONTH)] + " " + year;
        sharedPreferences = getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        budgetController = new BudgetController(context);
    }

    //task for the work manager
@NonNull
@Override
public Result doWork() {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean hasError = new AtomicBoolean(false);

    totalTransactions = 0;
    processedTransactions = 0;

    String budgetFormattedDate = sdf.format(new Date());

    //get the transactions that are repeat, and create new records using today's date
    transactionController.getAllTransactionsByDate(formattedDate, new ICallback() {
        @Override
        public void onSuccess(Object result) {
            try {
                transListArray = (JSONArray) result;
                totalTransactions = transListArray.length();

                if (totalTransactions == 0) {
                    //check if today is the last day of the month, then create the report
                    createExcelFileWhole();
                    deleteExpiredBudget(budgetFormattedDate);
                    latch.countDown();
                    return;
                }

                for (int i = 0; i < totalTransactions; i++) {
                    JSONObject transaction = transListArray.getJSONObject(i);
                    Transaction newTransaction = new Transaction(
                            Float.parseFloat(transaction.getString("amount")),
                            transaction.getString("description"),
                            formattedCurrDate,
                            false,
                            transaction.getString("categoryId"),
                            transaction.getString("image")
                    );

                    transactionController.createTransaction(newTransaction, new ICallback() {
                        @Override
                        public void onSuccess(Object result) {
                            processedTransactions++;
                            if (processedTransactions == totalTransactions) {
                                createExcelFileWhole();
                                deleteExpiredBudget(budgetFormattedDate);
                                latch.countDown();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            hasError.set(true);
                            Log.e("Transaction Error", "Error processing transaction: " + error);
                            latch.countDown();
                        }

                        @Override
                        public void onAuthFailure(String message) {
                            hasError.set(true);
                            Log.e("Auth Failure", "Authentication failed: " + message);
                            latch.countDown();
                        }
                    });
                }

            } catch (Exception e) {
                hasError.set(true);
                Log.e("Processing Error", "Exception in transaction processing", e);
                latch.countDown();
            }
        }

        @Override
        public void onError(String error) {
            hasError.set(true);
            Log.e("Transaction Fetch Error", "Error fetching transactions: " + error);
            latch.countDown();
        }

        @Override
        public void onAuthFailure(String message) {
            hasError.set(true);
            Log.e("Auth Failure", "Authentication failed while fetching transactions: " + message);
            latch.countDown();
        }
    });

    try {
        latch.await();
    } catch (InterruptedException e) {
        Log.e("Worker Interrupted", "Worker thread was interrupted", e);
        return Result.retry();
    }

    if (hasError.get()) {
        Log.e("Worker Failure", "Worker encountered an error and is returning failure.");
        return Result.failure();
    }

    //notify the user once the process completes
    if (totalTransactions > 0) {
        NotificationHelper.sendNotification(
                getApplicationContext(),
                "Recurring Bills Added",
                "Your recurring bills have been added successfully."
        );
    }

    return Result.success();
}

//create the excel, at the end of every month
    public void createExcelFile() {
        try {
            // Decode the token payload
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            // Parse the payload into a JSON object
            JSONObject payloadJson = new JSONObject(payload);
            String username = payloadJson.optString("username");  // Prevents exceptions

            // Prepare header and values
            String[] headers = {"Trakit Monthly Report", "Username:", "Month:", "", "Total Saved:", "Total Spent:"};
            String[] dataHeader = {"Date", "Category", "Type", "Amount", "Recurring", ""};
            String[] headersValue = {"",username, monthYearSql, "", "$" + income, "$" + expense}; //change to monthYearSql

            // Create a new Excel workbook
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(monthYearSql +"-"+ "report"); //change to monthYearSql
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
            HSSFRow dataHeaderRow = sheet.createRow(7);
            // Write the headers and values to the sheet
            for (int i = 0; i < headers.length; i++) {
                HSSFRow row = sheet.createRow(i);
                HSSFCell headerCell = row.createCell(0);
                headerCell.setCellValue(headers[i]);
                HSSFCell valueCell = row.createCell(2);
                valueCell.setCellValue(headersValue[i]);
                HSSFCell dataheader = dataHeaderRow.createCell(i);
                dataheader.setCellValue(dataHeader[i]);
            }

            for (int i=0;i<dataResponse.length();i++){
                HSSFRow row = sheet.createRow(8+i);
                JSONObject obj = dataResponse.getJSONObject(i);
                row.createCell(0).setCellValue(StringUtils.convertDateFormat(obj.optString("trans_date")));
                row.createCell(1).setCellValue(obj.optString("categoryName"));
                row.createCell(2).setCellValue(obj.optString("categoryType"));
                row.createCell(3).setCellValue(obj.optString("amount"));
                row.createCell(4).setCellValue(obj.optBoolean("recurring"));
            }
            monthlyReport = new MonthlyReport(workbook, monthYearSql, workbookName); //change to monthYearSql
            monthlyReportController.createMonthlyReport(monthlyReport, new ICallback() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createExcelFileWhole(){
        if (today == lastDayOfMonth){
            walletController.getWalletData(selectedMonthYear, new ICallback() { //change to selectedMonthYear
                @Override
                public void onSuccess(Object result) {
                    try{
                        JSONObject responseObject = (JSONObject) result;
                        income = responseObject.getString("income");
                        expense = responseObject.getString("expense");
                        balance = responseObject.getString("balance");
                        Float floatBalance =Float.parseFloat((balance));
                        DecimalFormat df = new DecimalFormat("0.00");
                        df.setMaximumFractionDigits(2);
                        balance = df.format(floatBalance);
                        monthlyReportController.getMonthlyReportData(monthYearSql, new ICallback() { //change to monthYearSql
                            @Override
                            public void onSuccess(Object result) {
                                dataResponse = (JSONArray) result;
                                createExcelFile();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onAuthFailure(String message) {
                            }
                        });
                    } catch (Exception e){
                        Log.d("Error", e.getMessage());
                    }
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

    //check for any expired budget and remove them
    public void deleteExpiredBudget(String date){
        budgetController.getAllBudgetsByDate(date, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                try{
                    JSONArray expiredBudgets = (JSONArray) result;
                    for(int i=0;i<expiredBudgets.length();i++){
                        JSONObject transaction = expiredBudgets.getJSONObject(i);
                        String budgetId = transaction.getString("id");
                        budgetController.deleteBudget(budgetId, new ICallback() {
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
                } catch (Exception e){
                    e.printStackTrace();
                }
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