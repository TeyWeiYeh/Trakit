package workers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import data.network.ICallback;
import data.network.controller.TransactionController;
import domain.Transaction;
import helpers.NotificationHelper;
import mdad.localdata.trakit.R;

public class RecurringBillWorker extends Worker {

    private final TransactionController transactionController;
    private final String formattedDate;
    private final String formattedCurrDate;
    private int totalTransactions;
    private int processedTransactions;
    private JSONArray transListArray;

    public RecurringBillWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        transactionController = new TransactionController(context);
        Date currDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd", Locale.getDefault());
        formattedDate = df.format(currDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        formattedCurrDate = sdf.format(currDate);
    }

    @NonNull
    @Override
    public Result doWork() {
        final CountDownLatch latch = new CountDownLatch(1);
        final Result[] workerResult = {Result.success()};
        totalTransactions = 0; // Initialize to 0

        transactionController.getAllTransactionsByDate(formattedDate, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                try {
                    transListArray = (JSONArray) result;
                    totalTransactions = transListArray.length();
                    if (transListArray.length() == 0) {
                        workerResult[0] = Result.success();
                        latch.countDown();
                        return;
                    }

                    processedTransactions = 0;

                    for (int i = 0; i < transListArray.length(); i++) {
                        JSONObject transaction = transListArray.getJSONObject(i);
                        // Extract transaction details
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
                                    workerResult[0] = Result.success();
                                    Log.d("transList", String.valueOf(transListArray));
                                    latch.countDown();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                workerResult[0] = Result.failure();
                                latch.countDown();
                            }

                            @Override
                            public void onAuthFailure(String message) {
                                workerResult[0] = Result.failure();
                                latch.countDown();
                            }
                        });
                    }

                } catch (Exception e) {
                    workerResult[0] = Result.failure();
                    latch.countDown();
                }
            }

            @Override
            public void onError(String error) {
                workerResult[0] = Result.failure();
                latch.countDown();
            }

            @Override
            public void onAuthFailure(String message) {
                workerResult[0] = Result.failure();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.retry();
        }

        // Send notification only if successful and transactions were processed
        if (Objects.equals(workerResult[0], Result.success()) && transListArray.length() > 0) {
            NotificationHelper.sendNotification(
                    getApplicationContext(),
                    "Recurring Bills Added",
                    "Your recurring bills have been added successfully."
            );
        }

        return workerResult[0];
    }

    private void exportcsv(){
        Date currentTime = Calendar.getInstance().getTime();
        String fileName = "my_data.csv";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try (FileWriter fileWriter = new FileWriter(file);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) { // Use Apache Commons CSV or OpenCSV

            // Sample Data - Replace with actual data
            String[] header = {"ID", "Name", "Age"};
            String[] row1 = {"1", "Alice", "24"};
            String[] row2 = {"2", "Bob", "30"};

            csvWriter.writeNext(header);
            csvWriter.writeNext(row1);
            csvWriter.writeNext(row2);

            Toast.makeText(getApplicationContext(), "CSV Exported: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }
}