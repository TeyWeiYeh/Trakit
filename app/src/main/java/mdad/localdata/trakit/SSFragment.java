package mdad.localdata.trakit;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mdad.localdata.trakit.splashscreen.SSFirstChildFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SSFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SSFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SSOneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SSFragment newInstance(String param1, String param2) {
        SSFragment fragment = new SSFragment();
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
        return inflater.inflate(R.layout.fragment_splashscreen, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        Fragment childFragment = new SSFirstChildFragment();
        //we are inside the parent fragment, to access the child fragment use getChildFragmentManager
        //if inside the child fragment, to access the parent use getParentFragmentManager
        FragmentManager fm = getChildFragmentManager();
        //begin the Fragment transaction after getting the FragmentManager
        FragmentTransaction transaction = fm.beginTransaction();
        //replace the fragment container with first child fragment
        transaction.replace(R.id.child_fragment_container, childFragment);
        //commit the transaction, sth like startActivity for intents
        transaction.commit();
    }
}