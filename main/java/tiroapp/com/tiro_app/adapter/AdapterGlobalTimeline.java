package tiroapp.com.tiro_app.adapter;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.HorlogeVIEW;
import tiroapp.com.tiro_app.controller.RowsGlobalTimeline;

/**
 * Created by user on 06/08/2015.
 */
public class AdapterGlobalTimeline  extends RecyclerView.Adapter<AdapterGlobalTimeline.ViewHolder> {

    private AgtInterface agtInterface;

    List<RowsGlobalTimeline> data = Collections.emptyList();
    Context context ;

    public AdapterGlobalTimeline(List<RowsGlobalTimeline> dataset) {
        this.data = dataset;
    }

    @Override
    public AdapterGlobalTimeline.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_global_timeline_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AdapterGlobalTimeline.ViewHolder holder, int position) {
        Integer timer = 9999;

        if(agtInterface != null) {
            holder.dataView = data.get(position);
            timer =  holder.dataView.timer - agtInterface.getCurrentHorlog();
            holder.tv_username.setText(holder.dataView.username);
            holder.dataPositionItem = position;
            holder.tv_rawData.setText(Html.fromHtml(holder.dataView.rawData));
            holder.tv_timer.setText(HorlogeVIEW.convertSecondeToReadable(timer));
            QuerysetAvatar(holder.dataView.username, holder);
            if(timer < 1){
                holder.tv_rawData.setText(Html.fromHtml(holder.dataView.rawData));
                holder.tv_rawData.setVisibility(View.GONE);
            }else{
                holder.tv_rawData.setText(Html.fromHtml(holder.dataView.rawData));
                holder.tv_rawData.setVisibility(View.VISIBLE);
            }

            if(holder.dataView.imAdder){
                int img = R.drawable.ic_image_timelapse_greenopposite;
                viewButtonPlus_1h_View(holder.tiroplus_1h, 7, true);
            }else{
                viewButtonPlus_1h_View(holder.tiroplus_1h, 0, false);

            }
        }

    }

    public void setListenner(AgtInterface clickListenner){
        this.agtInterface = clickListenner;
    }

    public void refresh(List<RowsGlobalTimeline> data){
        this.data = data;
        notifyDataSetChanged();
    }



    private void QuerysetAvatar(String username, final ViewHolder holder) {
        String URL = "http://tiro-app.com/user/avatar/uri/" + username;

        if (ApplicationController.getsInstance().getRequestQueue().getCache().get(URL) == null) {
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                holder.avatar.setImageUrl("http://tiro-app.com/user/avatar/" + response.getString("profilPicUri"), ApplicationController.getsInstance().getImageLoader());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            ApplicationController.getsInstance().addToRequestQueue(req, "GetAvatar");
        }else{
            Cache.Entry entry = ApplicationController.getsInstance().getRequestQueue().getCache().get(URL);
            try {
                JSONObject cachedData = new JSONObject(new String(entry.data, "UTF-8"));
                if(cachedData.getBoolean("success")){
                    holder.avatar.setImageUrl("http://tiro-app.com/user/avatar/" +cachedData.getString("profilPicUri") , ApplicationController.getsInstance().getImageLoader());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        Button Cgtl_btn_add_comment;
        TextView tv_username;
        TextView tv_rawData;
        TextView tv_timer;
        NetworkImageView avatar;
        Button tiroplus_1h;

        RowsGlobalTimeline dataView;


        Context context;
        public int dataPositionItem;

        public ViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();
            tv_username = (TextView) itemView.findViewById(R.id.Cgtl_textview_username);
            tv_rawData = (TextView) itemView.findViewById(R.id.Cgtl_textview_rawData);
            tv_timer = (TextView) itemView.findViewById(R.id.Cgtl_textview_timeLeft);
            avatar = (NetworkImageView) itemView.findViewById(R.id.Cgtl_image_username);
            tiroplus_1h = (Button) itemView.findViewById(R.id.Cgtl_btn_tiroplus_1h);
            Cgtl_btn_add_comment = (Button) itemView.findViewById(R.id.Cgtl_btn_add_comment);

            tiroplus_1h.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(agtInterface !=null) {
                        agtInterface.offer1hClicked(v, getAdapterPosition());
                        viewButtonPlus_1h_View(tiroplus_1h, 7, true);
                    }
                }
            });

            Cgtl_btn_add_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(agtInterface != null){
                        agtInterface.toComment(v, getAdapterPosition());
                    }
                }
            });
        }


    }

    private void viewButtonPlus_1h_View(Button button, int left, boolean green){
        if(green){
           // int img = R.drawable.ic_image_timelapse_greenopposite;
          //  button.setCompoundDrawablesWithIntrinsicBounds(img,0,0,0);
            button.setTextColor(context.getResources().getColor(R.color.GreenAccentColor));
            button.setText("thank you");

        }else if(!green){
          //  int img = R.drawable.ic_image_timelapse_greenopposite;
            button.setText(context.getResources().getString(R.string.button_plus_1h));
            button.setTextColor(context.getResources().getColor(R.color.GreenAccentColor));
         //   button.setCompoundDrawablesWithIntrinsicBounds(img,0,0,0);
        }

        float scale = context.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (left*scale + 0.5f);
        button.setPadding(-dpAsPixels,0,0,0);

    }

    public interface AgtInterface{
        int getCurrentHorlog();
        void toComment(View view, int position);
        void offer1hClicked(View view, int position);

    }
}
