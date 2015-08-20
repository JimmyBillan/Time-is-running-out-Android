package tiroapp.com.tiro_app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import tiroapp.com.tiro_app.adapter.AdapterSearchAddContact;
import tiroapp.com.tiro_app.controller.RowsSearch;

public class SearchContact_A extends AppCompatActivity implements AdapterSearchAddContact.ClickListenner {

    SharedPreferences pref;
    String tokenValue;

    protected RecyclerView mRecyclerView;
    protected AdapterSearchAddContact mAdapter;
    protected List<RowsSearch> dataset = Collections.emptyList();

    protected LinearLayoutManager mLayoutManager;
    protected View view;

    private EditText inputSearch;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
        if(pref.contains("JWToken")){ tokenValue = pref.getString("JWToken", "No_token");
            if(tokenValue.equals("No_token")){ startActivity(new Intent(this, LogIn_A.class));}
        }else{ startActivity(new Intent(this, LogIn_A.class)); }

        mRecyclerView = (RecyclerView) findViewById(R.id.A_Search_Contact_RecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        inputSearch = (EditText) findViewById(R.id.A_Search_Contact_inputSearch);
        btnSearch = (Button) findViewById(R.id.A_Search_Contact_btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input =inputSearch.getText().toString();
                if(input.length() >0){

                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SearchContact_A.this.getCurrentFocus().getWindowToken(), 0);
                    requestData(input);
                }
            }
        });
        createAdapterAndSet();

    }

    private void requestData(String usernameSearched) {
        String URL = "http://tiro-app.com/user/search/"+usernameSearched;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                List<RowsSearch> data = new ArrayList<>();
                                JSONArray arr = response.getJSONArray("users");
                                int count = arr.length();

                                for (int i = 0; i < count; i++){
                                    RowsSearch current = new RowsSearch();
                                    current.username = arr.getJSONObject(i).getString("username");
                                    if (!arr.getJSONObject(i).has("nbFollower")){
                                        current.nbFollower = 0;
                                    }else{
                                        current.nbFollower = arr.getJSONObject(i).getInt("nbFollower");
                                    }

                                    if(!arr.getJSONObject(i).has("profilPicUri")){
                                        current.profilPicUri = "null";
                                    }else{
                                        current.profilPicUri = arr.getJSONObject(i).getString("profilPicUri");
                                    }



                                    data.add(current);
                                }

                                if(count > 0){
                                    dataset = data;
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    createAdapterAndSet();
                                    mAdapter.refresh(dataset);

                                }else {
                                    dataset.clear();
                                    mRecyclerView.setVisibility(View.GONE);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                    toast.show();
                }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-access-token", tokenValue);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        ApplicationController.getsInstance().addToRequestQueue(req, "searchUser");

    }

    private void createAdapterAndSet(){
        mAdapter = new AdapterSearchAddContact(dataset);
        mAdapter.setListenner(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addContact(View view, int position) {

        String URL = "http://tiro-app.com/user/"+dataset.get(position).username+"/addcontact";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(!tokenValue.equals(response.getString("JWToken"))){
                                pref.edit().putString("JWToken", response.getString("JWToken")).commit();
                            }

                            if(response.getBoolean("success")){
                                Toast toast = Toast.makeText(getApplicationContext(), "New follow success", Toast.LENGTH_LONG);
                                toast.show();
                            }else{
                                Toast toast = Toast.makeText(getApplicationContext(), "New follow fail - You are already following this person", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
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

        ApplicationController.getsInstance().addToRequestQueue(req, "addContact");

    }
}
