package mdad.localdata.trakit.transactionfragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import data.network.ICallback;
import data.network.controller.TransactionController;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.R;
import utils.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewTransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewTransactionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ViewTransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewTransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewTransactionFragment newInstance(String param1, String param2) {
        ViewTransactionFragment fragment = new ViewTransactionFragment();
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
        return inflater.inflate(R.layout.fragment_view_transaction, container, false);
    }
    EditText etAmount, etDesc, etDate, etCatName;
    String type, amount, desc, date, catName, base64Img, id, recurring;
    //Boolean recurring;
    MaterialToolbar topAppBar;
    Button btnViewImage, closeButton, btnViewDelete, btnViewUpdate;
    ImageButton btnRecurring;
    ImageView transImg;
    TransactionController transactionController;
    Boolean boolRecc;
//    AutoCompleteTextView catDropdownListValue;
    public void onViewCreated(View view, Bundle savedInstanceState){
        //retrieve the data from the custom adapter
        Bundle retrieveInfo = getArguments();
        topAppBar = view.findViewById(R.id.topAppBar);
        type = retrieveInfo.getString("type");
        amount = retrieveInfo.getString("amount");
        desc = retrieveInfo.getString("desc");
        date = retrieveInfo.getString("date");
        catName = retrieveInfo.getString("catName");
        base64Img = retrieveInfo.getString("base64Img");
        id = retrieveInfo.getString("id");
        recurring = retrieveInfo.getString("recurring");
        boolRecc = Boolean.parseBoolean(recurring);
        transactionController = new TransactionController(getContext());

        etAmount = view.findViewById(R.id.etAmount);
        etDesc = view.findViewById(R.id.etViewDesc);
        etDate = view.findViewById(R.id.etViewDate);
        etCatName = view.findViewById(R.id.etViewCatName);
        btnViewImage = view.findViewById(R.id.btnViewImage);
        btnViewDelete = view.findViewById(R.id.btnViewDelete);
        btnViewUpdate = view.findViewById(R.id.btnViewUpdate);
        btnRecurring = view.findViewById(R.id.btnRecurring);
        etAmount.setText(amount);
        etDesc.setText(desc);
        etDate.setText(date);
        etCatName.setText(catName);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment goToAllTransFragment = new AllTransactionsFragment();
                FragmentManager fm = getParentFragmentManager();
                FragmentTransaction trans = fm.beginTransaction();
                trans.replace(R.id.trans_child_container, goToAllTransFragment);
                trans.commit();
            }
        });
        btnViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullScreenDialog();
            }
        });
        btnViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Delete")
                        .setMessage("Are you sure?")
                        .setNegativeButton(getResources().getString(R.string.btnCancel), (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton(getResources().getString(R.string.btnConfirm), (dialog, which) -> {
                            transactionController.deleteTransaction(id, new ICallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Fragment allTransFrag = new AllTransactionsFragment();
                                    FragmentManager fm = getParentFragmentManager();
                                    fm.beginTransaction().replace(R.id.trans_child_container, allTransFrag).commit();
                                    Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Failed to delete transaction", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onAuthFailure(String message) {
                                    Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                                    goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(goToLoginPage);
                                }
                            });
                        })
                        .setIcon(ContextCompat.getDrawable(getContext(), R.drawable.warning_icon))
                        .show();
            }
        });
        btnViewUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment goToUpdateTransFragment = new UpdateTransactionFragment();
                goToUpdateTransFragment.setArguments(retrieveInfo);
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.trans_child_container, goToUpdateTransFragment).commit();
            }
        });
        if (boolRecc){
            btnRecurring.setColorFilter(ContextCompat.getColor(getContext(), R.color.save));
        }
    }

    private void showFullScreenDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(getContext());

        // Set custom content for the dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_with_image, null);

        // Set up the dialog layout
        dialog.setContentView(dialogView);

        // Make the dialog full screen
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        transImg = dialogView.findViewById(R.id.transImg);
        transImg.setImageBitmap(ImageUtils.decodeBase64(base64Img));
        closeButton = dialogView.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}