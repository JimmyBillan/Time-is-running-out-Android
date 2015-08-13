package tiroapp.com.tiro_app.fragment;

import java.util.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.MainActivity;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.activity.Edit_post_A;
import tiroapp.com.tiro_app.activity.LogIn_A;
import tiroapp.com.tiro_app.adapter.AdapterPersonnalTimeline;
import tiroapp.com.tiro_app.controller.RowsPersonnalTimeline;

/**
 * Created by user on 22/07/2015.
 */
public class TL_personnal_F extends Fragment implements AdapterPersonnalTimeline.AptInterface {

    String tokenValue;
    Context context ;
    SharedPreferences pref ;
    Menu theMenu;
    private int horloge;



    SwipeRefreshLayout mySwiper;

    protected RecyclerView mRecyclerView;
    protected AdapterPersonnalTimeline mAdapter;
    protected List<RowsPersonnalTimeline> dataset = Collections.emptyList();

    protected RecyclerView.LayoutManager mLayoutManager;
    protected View view;

    public int getHorloge(){
        return this.horloge;
    }

    public void setmainHorloge(int horloge){
        this.horloge = horloge;
        Log.i("tl_perso", horloge+"");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FragmentActivity c = getActivity();

        setHasOptionsMenu(true);
        pref = getActivity().getSharedPreferences("application_credentials", Context.MODE_PRIVATE);

        if(pref.contains("JWToken")){
            tokenValue = pref.getString("JWToken", "No_token");
            if(tokenValue.equals("No_token")){
                startActivity(new Intent(getActivity(), LogIn_A.class));
            }
        }else{
            startActivity(new Intent(getActivity(), LogIn_A.class));
        }


        view = inflater.inflate(R.layout.fragment_tl_personnal, container, false);

        mySwiper = (SwipeRefreshLayout) view.findViewById(R.id.personnal_swipeRefreshLayout);
        mySwiper.setColorSchemeResources(R.color.refresh_swiper_color_3,R.color.refresh_swiper_color_2,R.color.refresh_swiper_color_1);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.drawerListPersonnal);
        mLayoutManager = new LinearLayoutManager(c);


        mRecyclerView.setLayoutManager(mLayoutManager);

        requestData();
        if(dataset.size() != 0){
            createAdapterAndSetIT();
        }


        mySwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                String URL = "http://tiro-app.com/user/avatar/uri/"+pref.getString("My_Username", "");;
                ApplicationController.getsInstance().getRequestQueue().getCache().invalidate(URL, true);
                requestData();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        theMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void createAdapterAndSetIT() {
        mAdapter = new AdapterPersonnalTimeline(dataset);
        mAdapter.setListenner(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void requestData(){

        String URL = "http://tiro-app.com/post/";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        readResponse(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getActivity(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                toast.show();
                handleErrorRequest();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-access-token", tokenValue);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        ApplicationController.getsInstance().addToRequestQueue(req, "GetPersonnalPost");

    }

    private void handleErrorRequest() {
        mySwiper.setRefreshing(false);
    }

    private void readResponse(JSONObject response){
        try{
            if(response.getBoolean("success")){
                String tempToken =response.getString("JWToken");
                if(!tokenValue.equals(tempToken)){
                    pref.edit().putString("JWToken", tempToken).commit();
                    tokenValue = tempToken;
                }

                readData(response);
            }else {
                pref.edit().remove("JWToken").commit();
                startActivity(new Intent(getActivity(), LogIn_A.class));
            }
        }catch (JSONException e){e.printStackTrace();}
    }

    private void readData(JSONObject response){
        List<RowsPersonnalTimeline> data = new ArrayList<>();

        try {
            JSONArray arr = response.getJSONArray("posts");
            int count = arr.length();
            for (int i = 0; i < count; i++) {
                RowsPersonnalTimeline current = new RowsPersonnalTimeline();
                current.username = arr.getJSONObject(i).getString("creator");
                current.rawData = arr.getJSONObject(i).getString("rawData");
                current.timer = Integer.parseInt(arr.getJSONObject(i).getString("timer"));
                current.id = arr.getJSONObject(i).getString("_id");
                current.dateCreation = Integer.parseInt(arr.getJSONObject(i).getString("dateCreation"));
                data.add(current);
            }

            if(count != 0){

                this.horloge = Integer.parseInt(response.getString("dateNow"));

                dataset = data;
                mRecyclerView.setVisibility(View.VISIBLE);
                createAdapterAndSetIT();
                mAdapter.refresh(dataset);
                mySwiper.setRefreshing(false);
                ((MainActivity)getActivity()).setmainHorloge(this.horloge);

            } else {
                dataset.clear();
                mRecyclerView.setVisibility(View.GONE);
                mySwiper.setRefreshing(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    @Override
    public void modifyClicked(View view, int position) {
        RowsPersonnalTimeline current = dataset.get(position);
        Intent intent = new Intent(getActivity(), Edit_post_A.class);

        intent.putExtra("EXTRA_POST_RAW_DATA", current.rawData);
        intent.putExtra("EXTRA_POST_ID", current.id);
        intent.putExtra("EXTRA_POST_POSITION", position);
        intent.putExtra("EXTRA_POST_TOTAL_TIMER_INT", pref.getInt("timerTotal_int_minutes", 0));
        intent.putExtra("EXTRA_POST_TOTAL_TIMER_String", pref.getString("timerTotal_String", "00h00"));
        startActivityForResult(intent, 1);
    }

    @Override
    public int getCurrentHorlog() {
        return ((MainActivity)getActivity()).getmainHorloge();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 1) {
            if (resultCode == getActivity().RESULT_OK) {
                dataset.get(data.getIntExtra("position", 0)).setRawData(data.getStringExtra("rawData"));
                mAdapter.notifyItemChanged(data.getIntExtra("position", 0));
            }
        }
    }
}
