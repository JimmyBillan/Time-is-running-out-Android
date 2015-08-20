package tiroapp.com.tiro_app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.HorlogeService;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.activity.List_follow;
import tiroapp.com.tiro_app.activity.LogIn_A;
import tiroapp.com.tiro_app.adapter.AdapterFollow;
import tiroapp.com.tiro_app.controller.RowsFollowing;
import tiroapp.com.tiro_app.db.UserProfilAdapter_db;
import tiroapp.com.tiro_app.interfaces.DialogClickListenerImagePicker;

/**
 * Created by user on 22/07/2015.
 */
public class Profil_F extends Fragment implements DialogClickListenerImagePicker {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    int serverResponseCode = 0;

    View view;
    Button btn_disconnect;

    SharedPreferences pref;
    String tokenValue;

    RecyclerView mRecyckerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<RowsFollowing> dataset = Collections.emptyList();

    private AdapterFollow mAdapter;

    private TextView tv_add_profil_pics, f_profil_textview_nbFollowing, f_profil_textview_username,f_profil_textview_nbFollower;
    private NetworkImageView f_profil_image_NImageView;

    private String  ppPath;
    private Uri selectedImageUri;
    Bitmap pp;

    SwipeRefreshLayout mySwiper;

    private LinearLayout F_profil_big_btn_following;
    private LinearLayout F_profil_big_btn_follower;

    private

    UserProfilAdapter_db userProfilHelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        pref = getActivity().getSharedPreferences("application_credentials", Context.MODE_PRIVATE);
        tokenValue = pref.getString("JWToken", "No_token");

    }

    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of LoginFragment");
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profil_f, container, false);


        f_profil_textview_nbFollowing = (TextView) view.findViewById(R.id.f_profil_textview_nbFollowing);
        f_profil_textview_nbFollower = (TextView) view.findViewById(R.id.f_profil_textview_nbFollower);
        f_profil_textview_username = (TextView) view.findViewById(R.id.f_profil_textview_username);


        f_profil_image_NImageView = (NetworkImageView) view.findViewById(R.id.f_profil_image_NImageView);
        f_profil_image_NImageView.setDefaultImageResId(R.drawable.ic_image_camera_blueaction_no_picture);

        btn_disconnect = (Button) view.findViewById(R.id.F_profil_btn_disconnect);
        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), HorlogeService.class));
                getActivity().getSharedPreferences("application_credentials", 0).edit().clear().commit();
                getActivity().finish();
            }
        });

        F_profil_big_btn_following = (LinearLayout) view.findViewById(R.id.F_profil_big_btn_following);
        F_profil_big_btn_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), List_follow.class);
                intent.putExtra("TYPE", "following");
                startActivity(intent);

            }
        });

        F_profil_big_btn_follower = (LinearLayout) view.findViewById(R.id.F_profil_big_btn_follower);
        F_profil_big_btn_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), List_follow.class);
                intent.putExtra("TYPE", "follower");
                startActivity(intent);
            }
        });

        tv_add_profil_pics =  (TextView) view.findViewById(R.id.f_profil_textview_upload_profil_pics);
        tv_add_profil_pics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialog();
            }
        });

        mySwiper = (SwipeRefreshLayout) view.findViewById(R.id.f_profil_swipeResfresh);
        mySwiper.setColorSchemeResources(R.color.refresh_swiper_color_3, R.color.refresh_swiper_color_2, R.color.refresh_swiper_color_1);

        mySwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getProfilData();
            }
        });




        getProfilData();
        return view;

    }


    private void displayDialog(){
        DialogFragment dialog = new PicImageMethode_F();
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "PicImage");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK ) {

                final File file = getTempFile(getActivity());
            try {
                pp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
                selectedImageUri = getImageUri(getActivity().getApplicationContext(), pp);

                ppPath =  getRealPathFromURI(selectedImageUri);
                new Thread(new Runnable() {
                    public void run() {

                        int response= uploadProfilPics();
                        System.out.println("RES : " + response);
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }




        } else
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && null != data) {

            selectedImageUri = data.getData();
            try {
                pp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                ppPath = getRealPathFromURI(selectedImageUri);


                new Thread(new Runnable() {
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                            }
                        });
                        int response= uploadProfilPics();
                        System.out.println("RES : " + response);
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getActivity().getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onGalleryClick() {

         Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(i, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onCameraClick() {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            Log.i("start activity", "true");
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(getActivity())) );
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    private File getTempFile(Context context){
        //it will return /sdcard/image.tmp
        final File path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
        if(!path.exists()){
            path.mkdir();
        }
        return new File(path, "tiro_profil_pics.tmp");
    }

    private int uploadProfilPics(){
        String upLoadServerUri = "http://tiro-app.com/user/avatar/update";
        String fileName = ppPath;
        Log.i("filename", fileName);

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
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            conn.setRequestProperty("x-access-token", tokenValue);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
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
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();

            if(serverResponseCode == 200){
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                                Toast.makeText(getActivity(), "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        getProfilData();
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


        }
        return serverResponseCode;
    }

    public void getProfilData() {
        final String username = pref.getString("My_Username", "#ERROR#");
     /*   UserProfilAdapter_db userProfilHelper = new UserProfilAdapter_db(getActivity());
        ContentValues userOffline = userProfilHelper.getUserProfil(username);

        if(userOffline !=null){
           Log.i("username  ",userOffline.getAsString("Username"));
           Log.i("NbFollower  ",userOffline.getAsString("NbFollower"));
           Log.i("NbFollowing  ",userOffline.getAsString("NbFollowing"));
           Log.i("Description  ", userOffline.getAsString("Description") + " ");
        }
    */

        if(!username.equals("#ERROR#")){
            String URL = "http://tiro-app.com/user/"+username;
            JSONObject cached;

                if(ApplicationController.getsInstance().getRequestQueue().getCache().get(URL) != null ){

                    try {
                        cached = new JSONObject(new String(ApplicationController.getsInstance().getRequestQueue().getCache().get(URL).data,"UTF-8"));
                        DisplayProfil(cached);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }


            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response) {
                            DisplayProfil(response);
                        }
                    }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast toast = Toast.makeText(getActivity(), "Check your connection or Server problem check out our twitter", Toast.LENGTH_LONG);
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

            ApplicationController.getsInstance().addToRequestQueue(req, "GetPersonnalPost");
        }

    }

    public void DisplayProfil(JSONObject response) {
        try {
            if (response.getBoolean("success")) {

             //   UserProfilAdapter_db userProfilHelper = new UserProfilAdapter_db(getActivity());
              //  ContentValues userWithoutKey = new ContentValues();


                String tempToken = response.getString("JWToken");
                if (!tokenValue.equals(tempToken)) {
                    pref.edit().putString("JWToken", tempToken).commit();
                    tokenValue = tempToken;
                }
                JSONObject data = response.getJSONObject("userData");
                try {
                    f_profil_textview_nbFollowing.setText(Integer.toString(data.getInt("nbFollowing")));
                 //   userWithoutKey.put("nbFollowing", Integer.toString(data.getInt("nbFollowing")));
                }catch (JSONException e){
                    e.printStackTrace();
                }
                try {
                    f_profil_textview_nbFollower.setText(Integer.toString(data.getInt("nbFollower")));
                  //  userWithoutKey.put("NbFollower", Integer.toString(data.getInt("nbFollower")));
                }catch (JSONException e){
                    e.printStackTrace();
                }
                f_profil_textview_username.setText(data.getString("username"));
                try {
                    f_profil_image_NImageView.setImageUrl("http://tiro-app.com/user/avatar/" + data.getString("profilPicUri"), ApplicationController.getsInstance().getImageLoader());
                }catch (JSONException e) {
                    e.printStackTrace();
                }

               // Long cursor = userProfilHelper.insertAppUser(data.getString("username"), userWithoutKey);
            } else {
                pref.edit().remove("JWToken").commit();
                startActivity(new Intent(getActivity(), LogIn_A.class));
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

