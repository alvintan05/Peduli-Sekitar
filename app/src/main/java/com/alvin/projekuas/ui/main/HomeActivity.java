package com.alvin.projekuas.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.alvin.projekuas.R;
import com.alvin.projekuas.databinding.ActivityHomeBinding;
import com.alvin.projekuas.ui.main.myreport.MyReportFragment;
import com.alvin.projekuas.ui.main.profile.ProfileActivity;
import com.alvin.projekuas.ui.main.home.HomeFragment;
import com.alvin.projekuas.ui.main.map.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initial fragment
        if (getSupportActionBar()!=null) getSupportActionBar().setTitle("Home");
        initialFragment();

        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Home");
                        initialFragment();
                        return true;

                    case R.id.navigation_my_report:
                        if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Report");
                        MyReportFragment report = new MyReportFragment();
                        FragmentTransaction transactionReport = getSupportFragmentManager().beginTransaction();
                        transactionReport.replace(R.id.content, report);
                        transactionReport.commit();
                        return true;

                    case R.id.navigation_map:
                        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Maps");
                        MapFragment map = new MapFragment();
                        FragmentTransaction transactionMaps = getSupportFragmentManager().beginTransaction();
                        transactionMaps.replace(R.id.content, map);
                        transactionMaps.commit();
                        return true;
                }
                return false;
            }
        });

    }

    private void initialFragment() {
        HomeFragment home = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, home);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_profile:
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
