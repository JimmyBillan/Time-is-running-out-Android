package tiroapp.com.tiro_app.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.activity.LogIn_A;
import tiroapp.com.tiro_app.adapter.AdapterGlobalTimeline;
import tiroapp.com.tiro_app.controller.RowsGlobalTimeline;
import tiroapp.com.tiro_app.controller.RowsPersonnalTimeline;

/**
 * Created by user on 22/07/2015.
 */
public class TL_global_F extends Fragment {

    String tokenValue;
    Context context ;
    SharedPreferences pref ;
    Menu theMenu;

    protected View view;
    SwipeRefreshLayout mySwiper;

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    protected AdapterGlobalTimeline mAdapter;
    protected List<RowsGlobalTimeline> dataset = Collections.emptyList();

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
                data.add(current);
            }
            pref.edit().putInt("dateNow", Integer.parseInt(response.getString("dateNow")) ).commit();
            if(count != 0){
                dataset = data;
                mRecyclerView.setVisibility(View.VISIBLE);
                createAdapterAndSetIT();
                mAdapter.refresh(dataset);
                mySwiper.setRefreshing(false);
            } else {
                dataset.clear();
                //mAdapter.clean();
                //mAdapter.refresh(dataset);
                mRecyclerView.setVisibility(View.GONE);

                mySwiper.setRefreshing(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
