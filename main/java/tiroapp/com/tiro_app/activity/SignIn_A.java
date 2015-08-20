package tiroapp.com.tiro_app.activity;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.controller.FormSignLog_C;
import tiroapp.com.tiro_app.R;


public class SignIn_A extends AppCompatActivity {

    FormSignLog_C checker ;


    RadioGroup radioSexGroup;
    RadioButton radioM;
    RadioButton radioF;

    TextView mail;
    TextView username;
    TextView password;
    TextView age;

    TextView error_mail;
    TextView error_username;
    TextView error_password;
    TextView error_age;
    TextView error_sex;

    ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        mail = (TextView) findViewById(R.id.mail);
        age = (TextView) findViewById(R.id.age);
        radioSexGroup = (RadioGroup) findViewById(R.id.radioSexGrp);
        radioM = (RadioButton) findViewById(R.id.radioM);
        radioF = (RadioButton) findViewById(R.id.radioF);

        error_mail = (TextView) findViewById(R.id.error_mail);
        error_username = (TextView) findViewById(R.id.error_username);
        error_password = (TextView) findViewById(R.id.error_password);
        error_age = (TextView) findViewById(R.id.error_age);
        error_sex = (TextView) findViewById(R.id.error_radioSex);

        pb = (ProgressBar) findViewById(R.id.progressBar);

       // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


}


    public void submitForm(View V){


        checker = new FormSignLog_C(this);
        checker.checkMail(mail.getText().toString().replace(" ", ""), error_mail);
        checker.checkUsername(username.getText().toString(), error_username);
        checker.checkPassword(password, error_password);
        checker.checkSex(radioSexGroup, error_sex);
        checker.checkAge(age, error_age);


        if(checker.theReturn) {
            if (checkNetworkConnection()) {
                String URL = "http://tiro-app.com/user/create";

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("mail", checker.mail);
                params.put("username", checker.username);
                params.put("gender", checker.gender);
                params.put("password", checker.password);
                params.put("age", checker.age);

                JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    if(response.getBoolean("success")){
                                        readResponse(checker.mail, checker.password);
                                    }else if(!response.getBoolean("success")){
                                        Toast toast = Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                                catch (JSONException e){e.printStackTrace();}
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                Toast toast = Toast.makeText(getApplicationContext(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                );



                ApplicationController.getsInstance().addToRequestQueue(req, "SignInPost");
            }
        }
    }

    public void readResponse(String mail, String password){
        Intent intent = new Intent(this, LogIn_A.class);
        intent.putExtra("EXTRA_MAIL", mail);
        intent.putExtra("EXTRA_PASSWORD", password);
        startActivity(intent);
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

    public void hideKeyboard(View V){
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in_, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onStop(){
        super.onStop();
        ApplicationController.getsInstance().cancelPendingRequests("SignInPost");
    }


}


