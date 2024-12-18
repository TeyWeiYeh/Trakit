package mdad.localdata.trakit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            // Token exists, navigate to MainActivity
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish(); // Close the landing activity
        }
        viewPager = (ViewPager2) findViewById(R.id.mypager);
        pagerAdapter = new LandingActivity.MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
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
                    //return LogoFragment.newInstance("fragment logo",null);
                    return SSFragment.newInstance("fragment ss", null);
                }
                case 1: {
                    //return LogoFragment.newInstance("fragment logo",null);
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