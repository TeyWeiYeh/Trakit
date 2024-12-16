package mdad.localdata.trakit;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class LandingActivity extends AppCompatActivity {
    public static ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private static final int NUM_PAGES = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager = (ViewPager2) findViewById(R.id.mypager);
        pagerAdapter = new LandingActivity.MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new Handler().postDelayed(() -> viewPager.setCurrentItem(1, true), 1500);
    }
    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(FragmentActivity fa) {
            super(fa);
        }
        @Override
        // to return the selected fragment depending on which tab is selected
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: {
                    return LogoFragment.newInstance("fragment logo",null);
                }
                case 1: {
                    return SSFragment.newInstance("fragment ss", null);
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