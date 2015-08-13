package tiroapp.com.tiro_app.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.Horloge;

public class Edit_post_A extends AppCompatActivity {

    public static final String reqCreate = "create";
    public static final String reqModify = "modify";

    private Menu menu;

    String rawData, timerTotal_string;
    Integer positionItem, timerTotal_int;

    Integer timerM = 0, timerH = 0, timerD = 0;

    SharedPreferences pref;

    String tokenValue;

    TextView tV_RawData, tV_timerTotal;

    String id_Post;
    String postMethode;

    SeekBar seekMinutes, seekHours,seekDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_);
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        tV_RawData = (TextView)findViewById(R.id.A_Edit_Post_tv_rawData);
        tV_timerTotal = (TextView)findViewById(R.id.A_Edit_Post_timerTotal);

        seekDays = (SeekBar)findViewById(R.id.A_Edit_Post_seekBarDays);
        seekHours = (SeekBar)findViewById(R.id.A_Edit_Post_seekBarHours);
        seekMinutes = (SeekBar) findViewById(R.id.A_Edit_Post_seekBarMinutes);


        pref = this.getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
        if(pref.contains("JWToken")){
            tokenValue = pref.getString("JWToken", "No_token");
            pref.edit().putString("JWToken", tokenValue).commit();
            if(tokenValue.equals("No_token")){
                startActivity(new Intent(Edit_post_A.this, LogIn_A.class));
            }
        }else{
            startActivity(new Intent(Edit_post_A.this, LogIn_A.class));
        }



        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            rawData = bundle.getString("EXTRA_POST_RAW_DATA");
            postMethode = reqModify;
            id_Post = bundle.getString("EXTRA_POST_ID");
            positionItem = bundle.getInt("EXTRA_POST_POSITION");

            tV_RawData.setText(rawData);
            timerTotal_int = bundle.getInt("EXTRA_POST_TOTAL_TIMER_INT");
            timerTotal_string = bundle.getString("EXTRA_POST_TOTAL_TIMER_String");
        }else{

                postMethode = reqCreate;
                timerTotal_string = pref.getString("timerTotal_String","00h00");
                timerTotal_int = pref.getInt("timerTotal_int_minutes", 0);

                setTitle("New post");

        }



        tV_timerTotal.setText(timerTotal_string);

        seekMinutes.setMax(59);
        seekMinutes.setProgress(0);
        seekMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                timerM = progress;
                updateMenuTimer(Horloge.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(Horloge.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
                OutOfTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekHours.setMax(23);
        seekHours.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timerH = progress * 60;
                updateMenuTimer(Horloge.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(Horloge.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
                OutOfTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekDays.setMax(29);
        seekDays.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timerD = progress * 60 * 24;
                updateMenuTimer(Horloge.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(Horloge.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
                OutOfTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void OutOfTime() {
       if( (timerTotal_int - (timerD + timerH + timerM)) < 0 ){
           tV_timerTotal.setTextColor(getResources().getColor(R.color.error_color));
       }else{
           tV_timerTotal.setTextColor(getResources().getColor(R.color.blue_textview));
       }
    }


    @Override
    public void onPause(){
        super.onPause();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Edit_post_A.this.getCurrentFocus().getWindowToken(), 0);
    }

    private void updateMenuTimer(String timer){
        MenuItem menuItemTimer = menu.findItem(R.id.A_Edit_Post_item_timerPost);
        menuItemTimer.setTitle(timer);
    }

    public void queryPost(){

        String URL = "http://tiro-app.com/post/"+postMethode;
        HashMap<String, String> params = new HashMap<>();
        params.put("rawData" , rawData);

        if(postMethode.equals(reqModify)){
            params.put("id", id_Post);
        }else{
            Log.i("on creer","j");
            params.put("timer", Integer.toString(timerD + timerH + timerM));
        }

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        readResponse(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
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



    private void readResponse(JSONObject response) {
        JSONObject dataUpdated;
        try {
            if( !response.isNull("success") && response.has("success")){
                if(response.getBoolean("success")){
                    if(!tokenValue.equals(response.getString("JWToken"))){
                        pref.edit().putString("JWToken", response.getString("JWToken")).commit();
                    }
                    if(!response.isNull("posts")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Post modified", Toast.LENGTH_LONG);
                        toast.show();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("position", positionItem);
                        returnIntent.putExtra("rawData",rawData);
                        setResult(RESULT_OK, returnIntent);
                        finish();

                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), "New post success", Toast.LENGTH_LONG);
                        toast.show();
                        onBackPressed();
                    }

                }

            }else{
                handleErrorRequest();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleErrorRequest() {
        Toast toast = Toast.makeText(getApplicationContext(), "Error ;S", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater =  getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.menu_edit_post_, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.A_Edit_Post_item_send) {
            postValidator();
            return true;
        }
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

    private void postValidator(){
        rawData = tV_RawData.getText().toString();
        if(rawData.length() == 0){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.A_Edit_Post_Toast_Raw_Data_Empty, Toast.LENGTH_LONG);
            toast.show();
        } else{
            queryPost();
        }
    }


}
