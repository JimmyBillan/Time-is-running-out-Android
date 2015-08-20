package tiroapp.com.tiro_app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import tiroapp.com.tiro_app.controller.RowsPersonnalTimeline;

/**
 * Created by user on 23/07/2015.
 */
public class AdapterPersonnalTimeline extends RecyclerView.Adapter<AdapterPersonnalTimeline.ViewHolder> {
    public final static String EXTRA_RAW_DATA = "com.tiro-app.RAW_DATA";
    public final static String EXTRA_ID_POST = "com.tiro-app.ID_POST";
    public final static String EXTRA_TIMER_DATA =  "com.tiro-app.TIMER";

    private AptInterface aptInterface;

    List<RowsPersonnalTimeline> data = Collections.emptyList();
    Integer position ;
    Integer timer;
    Context context ;

    public AdapterPersonnalTimeline(List<RowsPersonnalTimeline> data){
        this.data = data;
    }

    public void refresh(List<RowsPersonnalTimeline> data)
    {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_personnal_timeline_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.dataView= data.get(position);
        if(aptInterface != null){
            Integer timer =  holder.dataView.timer - aptInterface.getCurrentHorlog();
            this.position = position;

            if(timer > 1){
                holder.tv_username.setText(holder.dataView.username);
                holder.dataPositionItem = position;
                holder.tv_rawData.setText(Html.fromHtml(holder.dataView.rawData));
                holder.tv_timer.setText(HorlogeVIEW.convertSecondeToReadable(timer));

                if(timer < 60){
                    Log.i("timer", timer+"");
                    holder.tv_timer.setTextColor(context.getResources().getColor(R.color.error_color));
                }else{
                    holder.tv_timer.setTextColor(context.getResources().getColor(R.color.bluePolice));
                }
                QuerysetAvatar(holder.dataView.username, holder);
            }
        }
    }


    private void QuerysetAvatar(String username, final ViewHolder holder){
        String URL = "http://tiro-app.com/user/avatar/uri/"+username;

        if(ApplicationController.getsInstance().getRequestQueue().getCache().get(URL) == null){
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                holder.avatar.setImageUrl("http://tiro-app.com/user/avatar/"+response.getString("profilPicUri"),ApplicationController.getsInstance().getImageLoader());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener(){
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


    public void setListenner(AptInterface clickListenner){
        this.aptInterface = clickListenner;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rlt_layout_showing;
        RelativeLayout rlt_layout_hide;

        TextView tv_username;
        TextView tv_rawData;
        TextView tv_timer;
        TextView tv_comment;
        NetworkImageView avatar;
        Button editPost;
        RowsPersonnalTimeline dataView;
        Context context;
        public int dataPositionItem;


        public ViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();
            rlt_layout_showing  = (RelativeLayout) itemView.findViewById(R.id.Cptl_rlt_layout_showing);
            rlt_layout_hide = (RelativeLayout) itemView.findViewById(R.id.Cptl_rlt_layout_hide);
            tv_username = (TextView) itemView.findViewById(R.id.Cptl_textview_username);
            tv_rawData = (TextView) itemView.findViewById(R.id.Cptl_textview_rawData);
            tv_timer = (TextView) itemView.findViewById(R.id.Cptl_textview_timeLeft);

            tv_comment = (TextView) itemView.findViewById(R.id.Cptl_btn_add_comment);
            editPost    = (Button) itemView.findViewById(R.id.Cptl_btn_edit_post);
            avatar = (NetworkImageView) itemView.findViewById(R.id.Cptl_image_username);


            editPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(aptInterface != null){
                        aptInterface.modifyClicked(v, getAdapterPosition());
                    }
                }
            });

            tv_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(aptInterface != null){
                        aptInterface.toComment(v, getAdapterPosition());
                    }
                }
            });

        }


    }

    public interface AptInterface {
        void modifyClicked(View view, int position);
        void toComment(View view, int position);
        int getCurrentHorlog();

    }

}
