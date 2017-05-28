package com.devlovepreet.parkitlive.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.devlovepreet.parkitlive.R;
import com.devlovepreet.parkitlive.fragments.CenteredTextFragment;
import com.devlovepreet.parkitlive.fragments.FindParkingFragment;
import com.devlovepreet.parkitlive.fragments.FindMyCarFragment;
import com.devlovepreet.parkitlive.fragments.TestFragment;
import com.devlovepreet.parkitlive.menu.DrawerAdapter;
import com.devlovepreet.parkitlive.menu.DrawerItem;
import com.devlovepreet.parkitlive.menu.SimpleItem;
import com.devlovepreet.parkitlive.menu.SpaceItem;
import com.devlovepreet.parkitlive.utils.SessionManager;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by yarolegovich on 25.03.2017.
 */

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int POS_FIND_PARKING = 0;
    private static final int POS_FIND_MY_CAR = 1;
    private static final int POS_HISTORY = 2;
    private static final int POS_LOGOUT = 3;

    SlidingRootNav slidingRootNavLayout;
    String theme;
    FindParkingFragment myLocationFragment;
    TestFragment testFragment;
    private SessionManager session;
    private String[] screenTitles;

    //    private static final int PERMISSION_REQUEST_CODE = 200;
    private Drawable[] screenIcons;

    Calendar cStart, cEnd;
    int hourStart=0, hourEnd=0;
    String startTimeString="", endTimeString="";
    boolean flagIsStarted=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        theme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));
        if (theme.equals(getResources().getString(R.string.pref_theme_light_value))) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppTheme_Dark);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        slidingRootNavLayout = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();


        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        getSupportActionBar().setTitle(screenTitles[0]);

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_FIND_PARKING).setChecked(true),
                createItemFor(POS_FIND_MY_CAR),
                createItemFor(POS_HISTORY),
                createItemFor(POS_LOGOUT)));
        adapter.setListener(this);

        RecyclerView list = (RecyclerView) findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_FIND_PARKING);

//        if (!checkPermission()) {
//
//            requestPermission();
//
//        }


    }

    @Override
    public void onItemSelected(int position) {
        if (position == POS_FIND_PARKING) {

            Fragment selectedScreen = FindParkingFragment.createFor(screenTitles[position]);
            getSupportActionBar().setTitle(screenTitles[position]);
            showFragment(selectedScreen);

        } else if (position == POS_FIND_MY_CAR) {

            Fragment selectedScreen = FindMyCarFragment.createFor(screenTitles[position]);
            getSupportActionBar().setTitle(screenTitles[position]);
            showFragment(selectedScreen);

        } else if (position == POS_HISTORY) {
            Fragment selectedScreen = CenteredTextFragment.createFor(screenTitles[position]);
            getSupportActionBar().setTitle(screenTitles[position]);
            showFragment(selectedScreen);

        } else if (position == POS_LOGOUT) {
            logoutUser();
        }

//        Fragment selectedScreen = CenteredTextFragment.createFor(screenTitles[position]);
//        showFragment(selectedScreen);

        if (slidingRootNavLayout != null && !slidingRootNavLayout.isMenuHidden()) {
            slidingRootNavLayout.closeMenu();
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
//        getFragmentManager().beginTransaction()
//                .replace(R.id.container, fragment)
//                .commit();
    }

    private DrawerItem createItemFor(int position) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));
        if (theme.equals(getResources().getString(R.string.pref_theme_light_value))) {
            return new SimpleItem(screenIcons[position], screenTitles[position])
                    .withIconTint(color(R.color.textColorSecondary))
                    .withTextTint(color(R.color.textColorPrimary))
                    .withSelectedIconTint(color(R.color.colorAccent))
                    .withSelectedTextTint(color(R.color.colorAccent));
        } else {
            return new SimpleItem(screenIcons[position], screenTitles[position])
                    .withIconTint(color(R.color.textColorSecondaryInverse))
                    .withTextTint(color(R.color.textColorPrimaryInverse))
                    .withSelectedIconTint(color(R.color.colorAccentInverse))
                    .withSelectedTextTint(color(R.color.colorAccentInverse));
        }
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        dateFormatter.setLenient(false);
        Date startDate, endDate;


        if (id == R.id.action_settings) {

            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(myIntent);

            return true;
        }
        else if(id==R.id.action_start)
        {
            Toast.makeText(this, "Car Parked", Toast.LENGTH_SHORT).show();
            cStart=Calendar.getInstance();
            hourStart=cStart.get(Calendar.HOUR_OF_DAY);

            startDate=new Date();
            startTimeString = dateFormatter.format(startDate);
            flagIsStarted=true;
        }
        else if(id==R.id.action_end)
        {
            if(flagIsStarted==false) {
                Toast.makeText(this, "Invalid Operation", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            Toast.makeText(this, "Car Un-Parked, calculating Bill...", Toast.LENGTH_SHORT).show();
            cEnd=Calendar.getInstance();
            hourEnd=cEnd.get(Calendar.HOUR_OF_DAY);

            endDate=new Date();
            endTimeString = dateFormatter.format(endDate);

            int diffHour=hourEnd-hourStart;
            int price=20+diffHour*10;

            Intent i=new Intent();
            i.setClass(this, BillActivity.class);
            i.putExtra("startDate", startTimeString);
            i.putExtra("endDate", endTimeString);
            i.putExtra("bill", price);
            startActivity(i);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        String new_theme = sp.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));


        if (!theme.equals(new_theme)) {

            Intent intent = getIntent();
            finish();

            startActivity(intent);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_theme_key))) {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);

        return (result == PackageManager.PERMISSION_GRANTED);
    }

//    private void requestPermission() {
//
//        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0) {
//
//                    boolean accessLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//
//                    if (accessLocationAccepted) {
////                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
//                    } else {
//
////                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
//                                showMessageOKCancel(getResources().getString(R.string.toast_allow_acess),
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION},
//                                                            PERMISSION_REQUEST_CODE);
//                                                }
//                                            }
//                                        });
//                                return;
//                            }
//                        }
//
//                    }
//                }
//
//
//                break;
//        }
//    }
//
//
//    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
//        new AlertDialog.Builder(MainActivity.this)
//                .setMessage(message)
//                .setPositiveButton(getResources().getString(R.string.ad_ok), okListener)
//                .setNegativeButton(getResources().getString(R.string.ad_cancel), null)
//                .create()
//                .show();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == FindParkingFragment.MY_PERMISSIONS_REQUEST_LOCATION) {
            myLocationFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void logoutUser() {

        session.setLogin(false, "");
//
//        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
