package mdad.localdata.trakit.budgetfragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import data.network.ICallback;
import data.network.controller.BudgetController;
import domain.Budget;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateNewBudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateNewBudgetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateNewBudgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateNewBudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateNewBudgetFragment newInstance(String param1, String param2) {
        CreateNewBudgetFragment fragment = new CreateNewBudgetFragment();
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
        return inflater.inflate(R.layout.fragment_create_new_budget, container, false);
    }

    EditText etCreateName, etCreateAmt, etCreateStartDate, etCreateEndDate;
    Button btnCreateBudget;
    MaterialToolbar topAppBar;
    BudgetController budgetController;

    public void onViewCreated(View view, Bundle savedInstanceState){
        budgetController = new BudgetController(getContext());
        etCreateName = view.findViewById(R.id.etCreateName);
        etCreateAmt = view.findViewById(R.id.etCreateAmt);
        etCreateStartDate = view.findViewById(R.id.etCreateStartDate);
        etCreateEndDate = view.findViewById(R.id.etCreateEndDate);
        btnCreateBudget = view.findViewById(R.id.btnCreateBudget);
        topAppBar = view.findViewById(R.id.topAppBar);
        //navigate back to the all landing page
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment backToAllBudgetFragment = new AllBudgetsFragment();
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.home_fragment, backToAllBudgetFragment).commit();
            }
        });
        //date picker for start and end date period of budget
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .build();

        MaterialDatePicker<Long> endDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select End Date")
                .build();

        etCreateStartDate.setFocusable(false);
        etCreateStartDate.setClickable(true);

        etCreateEndDate.setFocusable(false);
        etCreateEndDate.setClickable(true);

        // Store selected dates
        final long[] startDate = {0};
        final long[] endDate = {0};

        etCreateStartDate.setOnClickListener(v -> {
            startDatePicker.show(getParentFragmentManager(), "START_DATE_PICKER");
        });

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            startDate[0] = selection;  // Save the selected start date

            // Format and set the date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etCreateStartDate.setText(sdf.format(calendar.getTime()));

            // If an end date is already selected, validate it
            if (endDate[0] != 0 && endDate[0] < startDate[0]) {
                etCreateEndDate.setText("");  // Clear invalid end date
                endDate[0] = 0;
                etCreateEndDate.setError("End date cannot be before start date");
                Toast.makeText(getContext(),"End date cannot be before start date", Toast.LENGTH_LONG).show();
            }
        });

        etCreateEndDate.setOnClickListener(v -> {
            endDatePicker.show(getParentFragmentManager(), "END_DATE_PICKER");
        });

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            endDate[0] = selection;  // Save the selected end date

            if (endDate[0] < startDate[0]) {
                etCreateEndDate.setText("");  // Clear invalid end date
                endDate[0] = 0;
                etCreateEndDate.setError("End date cannot be before start date");
                Toast.makeText(getContext(),"End date cannot be before start date", Toast.LENGTH_LONG).show();
            } else {
                etCreateEndDate.setError(null);
                // Format and set the date
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etCreateEndDate.setText(sdf.format(calendar.getTime()));
            }
        });
        //button on click to create new budget by calling the create budget function
        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = etCreateAmt.getText().toString();
                String name = etCreateName.getText().toString();
                String start_date = etCreateStartDate.getText().toString();
                String end_date = etCreateEndDate.getText().toString();
                if (amt.isEmpty() | name.isEmpty() | start_date.isEmpty() | end_date.isEmpty()){
                    Toast.makeText(getContext(),"Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                Budget budget = new Budget(Float.parseFloat(amt), name, start_date, end_date);
                createBudget(budget);
            }
        });
    }

    //reset the fields if successfully created budget
    public void createBudget(Budget budget){
        budgetController.createBudget(budget, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getContext(),result.toString(), Toast.LENGTH_LONG).show();
                etCreateAmt.setText("");
                etCreateName.setText("");
                etCreateStartDate.setText("");
                etCreateEndDate.setText("");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(),error, Toast.LENGTH_LONG).show();
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