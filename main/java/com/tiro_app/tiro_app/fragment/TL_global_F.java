package com.tiro_app.tiro_app.fragment;


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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tiro_app.tiro_app.ApplicationController;
import com.tiro_app.tiro_app.MainActivity;
import com.tiro_app.tiro_app.R;
import com.tiro_app.tiro_app.activity.Comment_post_A;
import com.tiro_app.tiro_app.activity.LogIn_A;
import com.tiro_app.tiro_app.adapter.AdapterGlobalTimeline;
import com.tiro_app.tiro_app.controller.RowsGlobalTimeline;

/**
 * Created by user on 22/07/2015.
 */
public class TL_global_F extends Fragment implements AdapterGlobalTimeline.AgtInterface{

    String tokenValue;
    Context context ;
    SharedPreferences pref ;
    Menu theMenu;

    protected View view;
    SwipeRefreshLayout mySwiper;

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    public AdapterGlobalTimeline mAdapter;
    public List<RowsGlobalTimeline> dataset = Collections.emptyList();

    private int horloge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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


        view = inflater.inflate(R.layout.fragment_tl_global, container, false);


        mySwiper = (SwipeRefreshLayout) view.findViewById(R.id.global_swipeRefreshLayout);
        mySwiper.setColorSchemeResources(R.color.refresh_swiper_color_3,R.color.refresh_swiper_color_2,R.color.refresh_swiper_color_1);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.drawerListglobal);
        mLayoutManager = new LinearLayoutManager(c);


        mRecyclerView.setLayoutManager(mLayoutManager);

        requestData();
        if(dataset.size() != 0){
            createAdapterAndSetIT();
        }


        mySwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
        return view;
    }

    private void createAdapterAndSetIT() {
        mAdapter = new AdapterGlobalTimeline(dataset);
        mAdapter.setListenner(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void requestData(){

        String URL = "http://tiro-app.com/post/following";

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
        List<RowsGlobalTimeline> data = new ArrayList<>();

        try {
            JSONArray arr = response.getJSONArray("posts");
            int count = arr.length();
            for (int i = 0; i < count; i++) {
                RowsGlobalTimeline current = new RowsGlobalTimeline();
                current.username = arr.getJSONObject(i).getString("creator");
                current.rawData = arr.getJSONObject(i).getString("rawData");
                current.timer = Integer.parseInt(arr.getJSONObject(i).getString("timer"));
                current.dateNow = Integer.parseInt(response.getString("dateNow"));
                current.id = arr.getJSONObject(i).getString("_id");
                current.imAdder = arr.getJSONObject(i).getBoolean("IamAdder");

                if(arr.getJSONObject(i).has("avatarUri")){
                    current.avatarUri = arr.getJSONObject(i).getString("avatarUri");
                }

                if(arr.getJSONObject(i).has("photoData")){
                    current.photoUri = arr.getJSONObject(i).getString("photoData");
                }
                data.add(current);
            }
            pref.edit().putInt("dateNow", Integer.parseInt(response.getString("dateNow")) ).commit();
            if(count != 0){
                dataset = data;
                this.horloge = Integer.parseInt(response.getString("dateNow"));
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
    public int getCurrentHorlog() {
        return ((MainActivity)getActivity()).getmainHorloge();
    }

    @Override
    public void toComment(View view, int position) {
        RowsGlobalTimeline current = dataset.get(position);
        Intent intent = new Intent(getActivity(), Comment_post_A.class);
        intent.putExtra("id", current.id);
        intent.putExtra("postAuthor", current.username);
        startActivityForResult(intent,2);
    }

    @Override
    public void offer1hClicked(View view, final int position) {
        final RowsGlobalTimeline current = dataset.get(position);
        String URL = "http://tiro-app.com/post/add1h";
        HashMap<String, String> params = new HashMap<>();
        params.put("id" , current.id);
        params.put("creator", current.username);

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("add1h").equals("success")){
                                dataset.get(position).imAdder = true;
                                refreshItemTimerQuery(response.getString("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
                return headers;
            }
        };
        ApplicationController.getsInstance().addToRequestQueue(req, "querySendPosts");

    }

    public void refreshItemTimerQuery(String id){
        String URL = "http://tiro-app.com/post/timer/"+id;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                                if(response.has("success")){
                                    if(response.getBoolean("success")){
                                        Integer size = dataset.size();
                                        Log.i("timer recu", response.getInt("timer")+"");
                                        Log.i("id recu", response.getString("id"));

                                        for(int i=0; i< size; i++) {
                                            if (dataset.get(i).id.equals(response.getString("id"))) {
                                                dataset.get(i).timer = response.getInt("timer");
                                            }
                                        }
                                    }else{
                                        Log.i("bool",response.getBoolean("success")+"");
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getActivity(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                toast.show();
                handleErrorRequest();
            }
        });
        ApplicationController.getsInstance().addToRequestQueue(req, "GetPersonnalPost");
        Log.i("id sent",id);


    }


}
