package com.tiro_app.tiro_app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.tiro_app.tiro_app.R;
import com.tiro_app.tiro_app.adapter.AdapterComment;
import com.tiro_app.tiro_app.controller.HorlogeVIEW;
import com.tiro_app.tiro_app.controller.RowsComment;

public class Comment_post_A extends AppCompatActivity {

    SharedPreferences pref;
    String tokenValue;

    protected RecyclerView mRecyclerView;
    protected AdapterComment mAdapter;
    protected List<RowsComment> dataset = Collections.emptyList();

    protected LinearLayoutManager mLayoutManager;
    protected View view;

    EditText inputComment;
    private ImageButton btnComment;

    String idPost, postAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_post_);

        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
        if(pref.contains("JWToken")){ tokenValue = pref.getString("JWToken", "No_token");
            if(tokenValue.equals("No_token")){ startActivity(new Intent(this, LogIn_A.class));}
        }else{ startActivity(new Intent(this, LogIn_A.class)); }

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            idPost = bundle.getString("id");
            postAuthor = bundle.getString("postAuthor");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.A_Comment_RecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        inputComment = (EditText) findViewById(R.id.A_Comment_EditText);

        btnComment = (ImageButton) findViewById(R.id.A_Comment_btn_Post);

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputComment.getText().toString();
                if (input.length() > 0) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Comment_post_A.this.getCurrentFocus().getWindowToken(), 0);
                    postComment(input);
                    inputComment.getText().clear();
                }
            }
        });
        createAdapterAndSet();
        getComment();
    }

    private void postComment(String input) {
        String URL = "http://tiro-app.com/post/comment/"+idPost+"/"+postAuthor;
        HashMap<String, String> params = new HashMap<>();
        params.put("rawData", input);

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("success")) {
                                if (response.getBoolean("success")) {
                                    String tempToken = response.getString("JWToken");
                                    if (!tokenValue.equals(tempToken)) {
                                        pref.edit().putString("JWToken", tempToken).commit();
                                        tokenValue = tempToken;
                                    }
                                    getComment();
                                }
                            }
                        }catch (JSONException e){

                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Post echec", Toast.LENGTH_LONG);
                toast.show();
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

    public void getComment() {
        String URL = "http://tiro-app.com/post/comment/"+ idPost+"/"+postAuthor;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.has("success")){
                               if(response.getBoolean("success")){
                                   String tempToken = response.getString("JWToken");
                                   if(!tokenValue.equals(tempToken)){
                                       pref.edit().putString("JWToken", tempToken).commit();
                                       tokenValue = tempToken;
                                   }

                                   List<RowsComment> data = new ArrayList<>();
                                   JSONArray arr = response.getJSONArray("comments");
                                   int count = arr.length();
                                   for (int i = 0; i < count; i++){
                                       RowsComment current = new RowsComment();
                                       current.username = arr.getJSONObject(i).getString("creator");
                                       current.id = arr.getJSONObject(i).getString("_id");
                                       current.comment = arr.getJSONObject(i).getString("commentText");

                                       if(!arr.getJSONObject(i).has("profilPicUri")){
                                           current.avatar = "null";
                                       }else{
                                           current.avatar = arr.getJSONObject(i).getString("profilPicUri");
                                       }

                                       if(!arr.getJSONObject(i).has("dateCreation")){
                                           current.avatar = "null";
                                       }else{
                                           Time time = new Time();
                                           time.setToNow();
                                           current.date = HorlogeVIEW.convertSecondeToReadable(response.getInt("dateNow") - arr.getJSONObject(i).getInt("dateCreation") );
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

        ApplicationController.getsInstance().addToRequestQueue(req, "getComment");
    }

    private void createAdapterAndSet(){
        mAdapter = new AdapterComment(dataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment_post_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();



        if(id ==android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {super.onBackPressed();
    }


}
