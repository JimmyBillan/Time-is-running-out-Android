package tiroapp.com.tiro_app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import tiroapp.com.tiro_app.adapter.AdapterFollowing;
import tiroapp.com.tiro_app.controller.RowsFollowing;

public class List_following extends AppCompatActivity {

    protected RecyclerView mRecyclerView;
    protected AdapterFollowing mAdapter;
    protected List<RowsFollowing> dataset = Collections.emptyList();

    protected LinearLayoutManager mLayoutManager;
    protected View view;

    SharedPreferences pref;
    String tokenValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_following);

        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
        if(pref.contains("JWToken")){ tokenValue = pref.getString("JWToken", "No_token");
            if(tokenValue.equals("No_token")){ startActivity(new Intent(this, LogIn_A.class));}
        }else{ startActivity(new Intent(this, LogIn_A.class)); }

        mRecyclerView = (RecyclerView) findViewById(R.id.A_List_following_RecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        requestDataforRecyclerFollowing();
        if(dataset.size() != 0) {
            createAdapterAndSet();
        }
    }


    private void createAdapterAndSet(){
        mAdapter = new AdapterFollowing(dataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void requestDataforRecyclerFollowing() {

        String URL = "http://tiro-app.com/user/following";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                String tempToken = response.getString("JWToken");
                                if(!tokenValue.equals(tempToken)){
                                    pref.edit().putString("JWToken", tempToken).commit();
                                    tokenValue = tempToken;
                                }
                                readDataforRecyclerFollowing(response);
                            }else if (response.getString("name").equals("JsonWebTokenError")){
                                pref.edit().remove("JWToken").commit();
                                startActivity(new Intent(List_following.this, LogIn_A.class));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(List_following.this, "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                toast.show();

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
        ApplicationController.getsInstance().addToRequestQueue(req, "GetFollowing");


    }

    private void readDataforRecyclerFollowing(JSONObject response){
        List<RowsFollowing> data = new ArrayList<>();

        JSONArray arr = null;
        try {
            arr = response.getJSONArray("listIamFollowing");
            int count = arr.length();
            for (int i = 0; i < count; i++ ){
                RowsFollowing current = new RowsFollowing();
                current.username = arr.getJSONObject(i).getString("username");
                current.nbFollower = arr.getJSONObject(i).getInt("nbWaiter");
                data.add(current);
            }

            if(count !=0){
                dataset = data;
                mRecyclerView.setVisibility(View.VISIBLE);
                createAdapterAndSet();
                mAdapter.refresh(dataset);
            }else {
                dataset.clear();
                mRecyclerView.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_list_following, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
