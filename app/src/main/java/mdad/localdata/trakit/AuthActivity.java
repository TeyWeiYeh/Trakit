package mdad.localdata.trakit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import mdad.localdata.trakit.authfragments.LoginFragment;

public class AuthActivity extends AppCompatActivity {
    public static ViewPager2 viewPager;
    //feeds the pager with the fragment views
    private FragmentStateAdapter pagerAdapter;
    //number of pages inside the fragment
    private static final int NUM_PAGES = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager = (ViewPager2) findViewById(R.id.authPager);
        viewPager.setUserInputEnabled(false);
        pagerAdapter = new MyPagerAdapter(this);
        //bind FragmentStateAdapter(pagerAdapter) to viewPager
        viewPager.setAdapter(pagerAdapter);
    }
    private class MyPagerAdapter extends FragmentStateAdapter{
        public MyPagerAdapter (FragmentActivity fa){
            super(fa);
        }
        @Override
        public Fragment createFragment(int position){
            switch (position){
                case 0: {
                    return new LoginFragment();
                }
                case 1: {
                    //return signup fragment
                }
                default:
                    return new Fragment();
            }
        }
        @Override
        // return count of tab items
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}