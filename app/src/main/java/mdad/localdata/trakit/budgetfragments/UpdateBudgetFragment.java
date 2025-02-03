package mdad.localdata.trakit.budgetfragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.appbar.MaterialToolbar;

import mdad.localdata.trakit.R;
import utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpdateBudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateBudgetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UpdateBudgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateBudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateBudgetFragment newInstance(String param1, String param2) {
        UpdateBudgetFragment fragment = new UpdateBudgetFragment();
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
        return inflater.inflate(R.layout.fragment_update_budget, container, false);
    }

    EditText etUpdateName, etUpdateAmt, etUpdateStartDate, etUpdateEndDate;
    String id, amt, start_date, end_date, name;
    MaterialToolbar topAppBar;

    public void onViewCreated(View view, Bundle savedInstanceState){
        Bundle bundle = getArguments();
        id = bundle.getString("id");
        name = bundle.getString("name");
        amt = bundle.getString("limit");
        start_date = bundle.getString("start_date");
        end_date = bundle.getString("end_date");
        etUpdateAmt = view.findViewById(R.id.etUpdateAmount);
        etUpdateName = view.findViewById(R.id.etUpdateName);
        etUpdateStartDate = view.findViewById(R.id.etUpdateStartDate);
        etUpdateEndDate = view.findViewById(R.id.etUpdateEndDate);
        topAppBar = view.findViewById(R.id.topAppBar);
        etUpdateAmt.setText(amt);
        etUpdateName.setText(name);
        etUpdateStartDate.setText(StringUtils.convertDateFormat(start_date));
        etUpdateEndDate.setText(StringUtils.convertDateFormat(end_date));
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment goToHomeFragment = new AllBudgetsFragment();
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.home_fragment, goToHomeFragment).commit();
            }
        });
    }
}