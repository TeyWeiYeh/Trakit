package mdad.localdata.trakit.splashscreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mdad.localdata.trakit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SSFirstChildFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SSFirstChildFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SSFirstChildFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SSFirstChildFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SSFirstChildFragment newInstance(String param1, String param2) {
        SSFirstChildFragment fragment = new SSFirstChildFragment();
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
        return inflater.inflate(R.layout.fragment_ss_firstchild, container, false);
    }
    Button btnToSSTwo;
    public void onViewCreated(View view, Bundle savedInstanceState){
        btnToSSTwo = (Button) view.findViewById(R.id.btnContinue1);
        btnToSSTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment secondChild = new SSSecondChildFragment();
                //get the parent fragment manager since we are in the child
                FragmentManager pfm = getParentFragmentManager();
                //start the transaction
                FragmentTransaction transaction = pfm.beginTransaction();
                transaction.replace(R.id.child_fragment_container, secondChild);
                transaction.commit();
            }
        });
    }
}