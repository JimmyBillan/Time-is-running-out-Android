
package com.tiro_app.tiro_app;


import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.tiro_app.tiro_app.activity.Edit_post_A;
import com.tiro_app.tiro_app.activity.LogIn_A;
import com.tiro_app.tiro_app.activity.SearchContact_A;
import com.tiro_app.tiro_app.controller.HorlogeVIEW;
import com.tiro_app.tiro_app.fragment.Profil_F;
import com.tiro_app.tiro_app.fragment.TL_global_F;
import com.tiro_app.tiro_app.fragment.TL_personnal_F;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener, HorlogeService.ServiceCallbacks {

    public static final short TIME_INTERVAL = 1;

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ActionBar actionBar;
    FloatingActionButton fab_addPOST;
    ViewPager mViewPager;
    SharedPreferences pref;
    String tokenValue;
    Menu theMenu;

    FragmentManager fm;

    private int mainHorloge = 0;

    private HorlogeService mService;
    private boolean bound = false;

    public int getmainHorloge(){
        return this.mainHorloge;
    }

    public void setmainHorloge(int horloge){
        this.mainHorloge = horloge;
    }


    @Override
    protected void onStart() {
        super.onStart();

        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);

        if(pref.contains("JWToken")){
            tokenValue = pref.getString("JWToken", "No_token");
            pref.edit().putString("JWToken", tokenValue).commit();
            if(tokenValue.equals("No_token")){
                startActivity(new Intent(MainActivity.this, LogIn_A.class));
            }
        }else{
            startActivity(new Intent(MainActivity.this, LogIn_A.class));
        }

        // bind to Service
        Intent intent = new Intent(this, HorlogeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // cast the IBinder and get MyService instance
            HorlogeService.LocalBinder binder = (HorlogeService.LocalBinder) service;
            mService =  binder.getService();
            bound = true;
            mService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Global");
        setContentView(R.layout.activity_main);


       startService(new Intent(this, HorlogeService.class));





        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        getSupportFragmentManager().popBackStack();

        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                actionBar.setSelectedNavigationItem(position);
            }
        });

        actionBar.addTab(
                actionBar.newTab()

                        .setIcon(R.drawable.ic_public_white_24dp)
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.ic_timer_white_24dp)
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.ic_person_white_24dp)
                        .setTabListener(this));

        fm = getSupportFragmentManager();

        getTotalTime();

        fab_addPOST = (FloatingActionButton) findViewById(R.id.fab_addPOST);

        fab_addPOST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Edit_post_A.class));
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        theMenu = menu;
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
       // super.onCreateOptionsMenu(menu);

        MenuItem menuItemTimer = menu.findItem(R.id.main_activity_item_timerTotal);
        menuItemTimer.setTitle(pref.getString("timerTotal_String", "00h00"));

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.main_activity_item_search:
                openSearch();
                return true;
            case R.id.main_activity_item_timerTotal:
                getTotalTime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openSearch() {
        startActivity(new Intent(MainActivity.this, SearchContact_A.class));
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        actionBar.setTitle(mAppSectionsPagerAdapter.getPageTitle(tab.getPosition()));

        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
//  Fragment selected = mAppSectionsPagerAdapter.getItem(tab.getPosition());
        //   selected.onCreate(Bundle new Bundle());

    }




    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:

                    return new TL_global_F();
                case 1:
                    return new TL_personnal_F();
                case 2:
                    return new Profil_F();

                default:
                    return new TL_global_F();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Tiro";
                case 1:
                    return "Your Posts";
                case 2:
                    return "Your Profil";
            }
            return Resources.getSystem().getString(R.string.tab_case_0);
        }
    }


    /*
    Refresh and set action bar time
     */

    public  void getTotalTime(){

        String URL = "http://tiro-app.com/user/time";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                String tempToken =response.getString("JWToken");
                                if(!tokenValue.equals(tempToken)){
                                    pref.edit().putString("JWToken", tempToken).commit();
                                    tokenValue = tempToken;
                                }

                                pref.edit().putInt("timerTotal_int_minutes", response.getInt("timerTotal")).commit();
                                edit_main_activity_item_timerTotal(HorlogeVIEW.converteMinutesToReadable(response.getInt("timerTotal")));
                            }else {
                                pref.edit().remove("JWToken").commit();
                                startActivity(new Intent(MainActivity.this, LogIn_A.class));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(MainActivity.this, "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                toast.show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("x-access-token", tokenValue);
                return headers;
            }
        };
        ApplicationController.getsInstance().addToRequestQueue(req, "GetPersonnalPost");

    }

    public void edit_main_activity_item_timerTotal(String readableTime){
        MenuItem menuItem = theMenu.findItem(R.id.main_activity_item_timerTotal);
        menuItem.setTitle(readableTime);
        pref.edit().putString("timerTotal_String", readableTime).commit();
    }

    @Override
    public void onBackPressed()
    {
    }

    @Override
    public void addTimeUnit() {
        setmainHorloge(getmainHorloge() + TIME_INTERVAL);
        TL_global_F frag = (TL_global_F) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+0);
        if(frag != null){
            if(frag.dataset.size() > 0 && frag.mAdapter != null){
                frag.mAdapter.notifyDataSetChanged();
            }
        }



    }


}


