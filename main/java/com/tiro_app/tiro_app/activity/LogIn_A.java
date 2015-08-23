package com.tiro_app.tiro_app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import com.tiro_app.tiro_app.ApplicationController;
import com.tiro_app.tiro_app.MainActivity;
import com.tiro_app.tiro_app.R;
import com.tiro_app.tiro_app.controller.FormSignLog_C;

public class LogIn_A extends AppCompatActivity {

    SharedPreferences pref;

    FormSignLog_C checker;
    EditText mail;
    EditText password;
    TextView error_mail;
    TextView error_password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
       if(pref.contains("JWToken")){
            startActivity(new Intent(LogIn_A.this, MainActivity.class));
        }

        setContentView(R.layout.activity_log_in_);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mail = (EditText) findViewById(R.id.mail);
        password = (EditText) findViewById(R.id.password);
        error_mail = (TextView) findViewById(R.id.error_mail);
        error_password = (TextView) findViewById(R.id.error_password);

        mail.setText("tirodev_ada0@example.com");
        password.setText("12345678");


        //case user sign in the app, transfert his mail and password to login activity  and set editext text
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            mail.setText(bundle.getString("EXTRA_MAIL"));
            password.setText(bundle.getString("EXTRA_PASSWORD"));
        }

        mail.setText("billan.jimmy@gmail.com");
        password.setText("12345678");
        TextView signIn = (TextView) findViewById(R.id.go_to_signin);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogIn_A.this, SignIn_A.class));
            }
        });

        TextView forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogIn_A.this, ForgotPassword_A.class));
            }
        });


    }
    public void submitForm(View V){
        checker = new FormSignLog_C(this);
        checker.checkMail(mail.getText().toString(), error_mail);
        checker.checkPassword(password, error_password);

        if(checker.theReturn){
            if (checkNetworkConnection()){
                String URL = "http://tiro-app.com/user/login";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("mail", checker.mail);
                params.put("password", checker.password);

                JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(response.getBoolean("success")){
                                    readReponse(response);
                                }else{
                                    Toast toast = Toast.makeText(getApplicationContext(), response.getString("input")+" : "+response.getString("why"), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }catch (JSONException e){e.printStackTrace();}
                        }
                    }, new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                            Log.i("Error : ", error.getMessage() + "");
                            Toast toast = Toast.makeText(getApplicationContext(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                            toast.show();
                    }
                });
                ApplicationController.getsInstance().addToRequestQueue(req, "loginPost");
            }
        }
    }

    public void readReponse(JSONObject response){
        try {
            SharedPreferences pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
            pref.edit().putString("JWToken", response.getString("JWToken")).commit();
            pref.edit().putString("My_Username", response.getString("Username")).commit();
           // UserProfilAdapter_db userProfilHelper = new UserProfilAdapter_db(this);
           // Long cursor = userProfilHelper.insertAppUser(response.getString("Username"), null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected void onStop(){
        super.onStop();
        ApplicationController.getsInstance().cancelPendingRequests("loginPost");
    }
    public boolean checkNetworkConnection() {
        boolean thereturn = true;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.no_wifi_or_mobile, Toast.LENGTH_SHORT);
            t.show();
            thereturn = false;
        }

        return thereturn;
    }

}
