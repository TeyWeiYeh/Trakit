package workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import data.network.ICallback;
import data.network.controller.TransactionController;
import domain.Transaction;

public class RecurringBillWorker extends Worker {

    private final TransactionController transactionController;
    private final String formattedDate;
    private final String formattedCurrDate;
    private int totalTransactions;
    private int processedTransactions;

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
        // Use CountDownLatch to wait for the async call to complete
        final CountDownLatch latch = new CountDownLatch(1);
        final Result[] workerResult = {Result.success()}; // Default to success

        // Call API to get all transactions by date
        transactionController.getAllTransactionsByDate(formattedDate, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                try {
                    JSONArray transListArray = (JSONArray) result;
                    totalTransactions = transListArray.length(); // Track the total number of transactions
                    processedTransactions = 0; // Initialize processed counter

                    for (int i = 0; i < transListArray.length(); i++) {
                        JSONObject transaction = transListArray.getJSONObject(i);
                        String transId = transaction.getString("transactionId");
                        String transAmount = transaction.getString("amount");
                        String transDescription = transaction.getString("description");
                        String transDateCreated = transaction.getString("date_created");
                        String transDateUpdated = transaction.getString("date_updated");
                        String transDate = transaction.getString("trans_date");
                        String transRecurring = transaction.getString("recurring");
                        String transBudgetId = transaction.getString("budgetId");
                        String transUserId = transaction.getString("userId");
                        String transImage = transaction.getString("image");
                        String transCatId = transaction.getString("categoryId");
                        String transCatName = transaction.getString("categoryName");
                        String transCatType = transaction.getString("categoryType");

                        Transaction newTransaction = new Transaction(Float.parseFloat(transAmount), transDescription, formattedCurrDate, false, transCatId, transImage);
                        transactionController.createTransaction(newTransaction, new ICallback() {
                            @Override
                            public void onSuccess(Object result) {
                                Log.d("Recurring success", (String) result + formattedCurrDate);

                                processedTransactions++; // Increment the processed counter
                                if (processedTransactions == totalTransactions) {
                                    workerResult[0] = Result.success(); // All transactions processed successfully
                                    latch.countDown();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                workerResult[0] = Result.failure(); // Mark as failure on error
                                latch.countDown();
                            }

                            @Override
                            public void onAuthFailure(String message) {
                                workerResult[0] = Result.failure(); // Mark as failure on auth failure
                                latch.countDown();
                            }
                        });
                    }

                    // If no transactions to process, immediately complete the work
                    if (totalTransactions == 0) {
                        workerResult[0] = Result.success();
                        latch.countDown();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    workerResult[0] = Result.failure(); // Mark as failure on exception
                    latch.countDown();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("RecurringBillWorker", "Error: " + error);
                workerResult[0] = Result.failure(); // Mark as failure on error
                latch.countDown(); // Signal that the work is done
            }

            @Override
            public void onAuthFailure(String message) {
                Log.e("RecurringBillWorker", "Auth Failure: " + message);
                workerResult[0] = Result.failure(); // Mark as failure on auth failure
                latch.countDown(); // Signal that the work is done
            }
        });

        try {
            latch.await(); // Block until callback is called
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.retry(); // Retry if interrupted
        }

        // Return the final result after the async task completes
        return workerResult[0];
    }
}
