
package fr.wildcodeschool.metro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static fr.wildcodeschool.metro.Helper.extractStation;

public class ListStation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static Settings mSettings;
    private Singleton settings;
    private Fragment mFragment;
    private RecyclerView recycleListStations;
    private ArrayList<Station> mStations = new ArrayList<>();
    private  TextView userNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_station);
        getSettings();
        navigationDrawer();
        extractStationList();
        switchActivity();
        setUserName();
    }

    private void setUserName(){
        FirebaseDatabase favoriteStationBase = FirebaseDatabase.getInstance();
        FirebaseAuth userAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.tvUserName);
        navUsername.setText(name);
        navUsername.setText(name);
    }

    private void switchActivity() {
        BottomNavigationView navigation = findViewById(R.id.navigation_list_station);
        navigation.getMenu().setGroupCheckable(0, true, true);
        navigation.setSelectedItemId(R.id.navigation_list);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void getSettings() {
        settings = Singleton.getInstance();
        mSettings = settings.getSettings();
    }

    private void importFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.drawer_layout, mFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void extractStationList() {
        extractStation(ListStation.this, mSettings, new Helper.BikeStationListener() {

            @Override
            public void onResult(ArrayList<Station> stations) {
                mStations = stations;
                recycleListStations = findViewById(R.id.list_recycle_station);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ListStation.this, LinearLayoutManager.VERTICAL, false);
                recycleListStations.setLayoutManager(layoutManager);
                StationsRecyclerAdapter adapter = new StationsRecyclerAdapter(stations);
                recycleListStations.setAdapter(adapter);

            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_station_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent goMapsAcitvity = new Intent(ListStation.this, MapsActivity.class);
            startActivity(goMapsAcitvity);
        } else if (id == R.id.nav_account) {
            mFragment = new AccountFragment();
            importFragment();
        } else if (id == R.id.nav_settings) {
            mFragment = new SettingsFragment();
            importFragment();
        } else if (id == R.id.nav_favorite) {
            mFragment = new FavoriteFragment();
            importFragment();
        } else if (id == R.id.nav_share) {
            mFragment = new ShareFragment();
            importFragment();
        } else if (id == R.id.nav_send) {
            mFragment = new SendFragment();
            importFragment();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent goMapsAcitvity = new Intent(ListStation.this, MapsActivity.class);
                    startActivity(goMapsAcitvity);
                    return true;
                case R.id.navigation_list:
                    return true;
            }
            return false;
        }
    };

}