package com.tiro_app.tiro_app.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.tiro_app.tiro_app.ApplicationController;
import com.tiro_app.tiro_app.R;
import com.tiro_app.tiro_app.controller.HorlogeVIEW;

public class Edit_post_A extends AppCompatActivity{

    public static final String reqCreate = "create";
    public static final String reqModify = "modify";


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    private Menu menu;

    String rawData, timerTotal_string;
    Integer positionItem, timerTotal_int;

    Integer timerM = 0, timerH = 0, timerD = 0;

    SharedPreferences pref;

    String tokenValue;

    TextView tV_RawData, tV_timerTotal, tV_timerPost;

    String id_Post;
    String postMethode;

    ImageButton display_time_seekbar_btn, gallery_btn, camera_btn,GPS_btn;
    Button btn_RemoveImage ;

    ImageView imagePreview;

    LinearLayout seek_conteneur;
    SeekBar seekMinutes, seekHours,seekDays;

    private String  ppPath;
    String mCurrentPhotoPath;
    private Uri selectedImageUri;


    Boolean canUploadPhoto = false;

    int serverResponseCode = 0;
    private int orientationPIC = 0;
    private Bitmap bitmapDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_);

        tV_RawData = (TextView)findViewById(R.id.A_Edit_Post_tv_rawData);
        tV_timerTotal = (TextView)findViewById(R.id.A_Edit_Post_timerTotal);
        tV_timerPost = (TextView) findViewById(R.id.A_Edit_Post_timerPost);

        seekDays = (SeekBar)findViewById(R.id.A_Edit_Post_seekBarDays);
        seekHours = (SeekBar)findViewById(R.id.A_Edit_Post_seekBarHours);
        seekMinutes = (SeekBar) findViewById(R.id.A_Edit_Post_seekBarMinutes);

        display_time_seekbar_btn =(ImageButton) findViewById(R.id.A_Edit_Post_display_time_seekbar);
        gallery_btn = (ImageButton) findViewById(R.id.A_Edit_Post_Gallery_btn);
        camera_btn = (ImageButton) findViewById(R.id.A_Edit_Post_Camera_btn);
        seek_conteneur = (LinearLayout) findViewById(R.id.A_Edit_Post_seek_conteneur);

        btn_RemoveImage = (Button) findViewById(R.id.A_Edit_Post_Button_RemoveImage);
        imagePreview = (ImageView) findViewById(R.id.A_Edit_Post_ImagePreview);

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

        btn_RemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImageDisplayed();
            }
        });

        display_time_seekbar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Edit_post_A.this.getCurrentFocus().getWindowToken(), 0);
                hideSoftKeyboard();
                if (seek_conteneur.getVisibility() == View.VISIBLE) {
                    seek_conteneur.setVisibility(View.GONE);
                } else {
                    seek_conteneur.setVisibility(View.VISIBLE);
                }
            }
        });

        gallery_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImageDisplayed();
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_GALLERY);
            }
        });

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImageDisplayed();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("error", "creating file");
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile) );
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });


        tV_timerTotal.setText(timerTotal_string);

        seekMinutes.setMax(59);
        seekMinutes.setProgress(0);
        seekMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                timerM = progress;
                updateMenuTimer(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerPost.setText(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(HorlogeVIEW.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
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
                updateMenuTimer(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerPost.setText(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(HorlogeVIEW.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
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
                updateMenuTimer(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerPost.setText(HorlogeVIEW.converteMinutesToReadable(timerD + timerH + timerM));
                tV_timerTotal.setText(HorlogeVIEW.converteMinutesToReadable(timerTotal_int - (timerD + timerH + timerM)));
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

    private void clearImageDisplayed(){
        if(bitmapDisplay != null){
            bitmapDisplay.recycle();
            bitmapDisplay = null;
            canUploadPhoto = false;
            orientationPIC = 0;
            imagePreview.setImageDrawable(null);
            ppPath = null;
            btn_RemoveImage.setVisibility(View.GONE);
        }
    }

    private void OutOfTime() {
       if( (timerTotal_int - (timerD + timerH + timerM)) < 0 ){
           tV_timerPost.setTextColor(getResources().getColor(R.color.error_color));
           tV_timerTotal.setTextColor(getResources().getColor(R.color.error_color));
       }else{
           tV_timerTotal.setTextColor(getResources().getColor(R.color.blue_textview));
           tV_timerPost.setTextColor(getResources().getColor(R.color.blue_textview));
       }
    }

    @Override
    public void onPause(){
        super.onPause();
        //((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Edit_post_A.this.getCurrentFocus().getWindowToken(), 0);
        hideSoftKeyboard();
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
            Log.i("on creer","j"+rawData);
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

    private void postValidator(){
        rawData = tV_RawData.getText().toString();
        if(rawData.length() == 0 && !canUploadPhoto){ // CONDITION POSITION FUTUR
            Toast toast = Toast.makeText(getApplicationContext(), R.string.A_Edit_Post_Toast_Raw_Data_Empty, Toast.LENGTH_LONG);
            toast.show();
        }else if((timerD + timerH + timerM) < 1){
            seek_conteneur.setVisibility(View.VISIBLE);
            //((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Edit_post_A.this.getCurrentFocus().getWindowToken(), 0);
            hideSoftKeyboard();
            Toast toast = Toast.makeText(getApplicationContext(), R.string.A_Edit_Post_Toast_No_TIMER_SET, Toast.LENGTH_LONG);
            toast.show();
        }else if(canUploadPhoto){
            new Thread(new Runnable() {
                public void run() {
                    int response= uploadPostPhoto();
                    System.out.println("RES : " + response);
                }
            }).start();
            Toast.makeText(getApplicationContext(), "Uploading ", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Log.i("rawData",rawData);
            queryPost();
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_Tiro_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK ) {

            ppPath =  mCurrentPhotoPath;
            btn_RemoveImage.setVisibility(View.VISIBLE);

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(ppPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmapDisplay = BitmapFactory.decodeFile(ppPath);
            rotateBitmap(orientationPIC);
            imagePreview.setImageBitmap(bitmapDisplay);
            orientationPIC = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
            canUploadPhoto = true;

        } else
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && null != data) {
                selectedImageUri = data.getData();
                try {
                    ppPath = getRealPathFromURI(selectedImageUri);
                    btn_RemoveImage.setVisibility(View.VISIBLE);
                    ExifInterface exif = new ExifInterface(ppPath);
                    orientationPIC = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    bitmapDisplay = decodeSampledBitmapFromPath(ppPath, 720, 1280);
                    rotateBitmap(orientationPIC);
                    imagePreview.setImageBitmap(bitmapDisplay);
                    canUploadPhoto = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }



        }else{
            canUploadPhoto = false;
        }

    }

    public static Bitmap decodeSampledBitmapFromPath(String ppPath,int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ppPath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(ppPath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void rotateBitmap(int orientation){
        Log.i("rotation avant", orientation+"");
        orientation = getRotate(orientation);
        Log.i("rotation", orientation+"");
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        bitmapDisplay = Bitmap.createBitmap(bitmapDisplay, 0,0,bitmapDisplay.getWidth(),bitmapDisplay.getHeight(),matrix,true);
    }

    public static int getRotate(int orientationPic) {
        switch (orientationPic) {
            case 1:
                return 0;
            case 2:
                return 0;
            case 3:
                return 180;
            case 4:
                return 180;
            case 5:
                return 80;
            case 6:
                return 90;
            case 7:
                return -90;
            case 8:
                return -90;
            default:
                return 0;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] filePathColumn  = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri,  filePathColumn, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndex(filePathColumn[0]);
        String path = cursor.getString(column_index);
        cursor.close();
        return path;

    }

    private int uploadPostPhoto(){
        String upLoadServerUri = "http://tiro-app.com/post/create";
        String fileName = ppPath;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File Does not exist");
            return 0;
        }

        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);

            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;charset=UTF-8;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            conn.setRequestProperty("x-access-token", tokenValue);
            conn.setRequestProperty("orientationpic", Integer.toString(orientationPIC));

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            Log.i("filename edit", fileName);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...


            dos.writeBytes(lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"orientationPic\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(Integer.toString(orientationPIC));
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"rawData\"" + lineEnd);
            dos.writeBytes(lineEnd);
            Log.i("rawdata", rawData);
            dos.writeBytes(rawData);
            dos.writeBytes(lineEnd);
           dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"timer\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(Integer.toString(timerD + timerH + timerM));
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();

            if(serverResponseCode == 200){
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                Log.i("json retour", sb.toString());
                this.runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "File Upload Complete.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "File Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ex) {
                }
            }
        }
        return serverResponseCode;
    }

    public void hideSoftKeyboard(){
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

}
