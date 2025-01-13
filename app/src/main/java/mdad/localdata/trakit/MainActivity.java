package mdad.localdata.trakit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import mdad.localdata.trakit.mainfragments.AddFragment;
import mdad.localdata.trakit.mainfragments.CategoryFragment;
import mdad.localdata.trakit.mainfragments.ChartFragment;
import mdad.localdata.trakit.mainfragments.HomeFragment;
import mdad.localdata.trakit.mainfragments.TransactionFragment;

public class MainActivity extends AppCompatActivity {
    //handles the navigation and swiping gestures of the fragments
    private static final int NUM_PAGES = 2;
    //172.30.33.55
    public final static String ipBaseUrl = "http://192.168.18.18/project/api";
    BottomNavigationView bottomNavigationView;
    MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        HomeFragment homeFragment = new HomeFragment();
        ChartFragment chartFragment = new ChartFragment();
        AddFragment addFragment = new AddFragment();
        TransactionFragment transactionFragment = new TransactionFragment();
        CategoryFragment categoryFragment = new CategoryFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.main_fragment_container, homeFragment);
        transaction.commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    fm.beginTransaction()
                            .replace(R.id.main_fragment_container, homeFragment)
                            .commit();
                    return true;
                } else if (itemId == R.id.chart) {
                    fm.beginTransaction()
                            .replace(R.id.main_fragment_container, chartFragment)
                            .commit();
                    return true;
                } else if (itemId == R.id.add) {
                    fm.beginTransaction()
                            .replace(R.id.main_fragment_container, addFragment)
                            .commit();
                    return true;
                }else if (itemId == R.id.trans) {
                    fm.beginTransaction()
                            .replace(R.id.main_fragment_container, transactionFragment)
                            .commit();
                    return true;
                }else if (itemId == R.id.category) {
                    fm.beginTransaction()
                            .replace(R.id.main_fragment_container, categoryFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });
    }
    @Override
//add the option menu to the activity
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the option menu and display the option items when clicked;
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }
}