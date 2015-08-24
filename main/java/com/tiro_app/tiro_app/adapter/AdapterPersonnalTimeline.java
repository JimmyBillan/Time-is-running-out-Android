package com.tiro_app.tiro_app.adapter;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

import com.tiro_app.tiro_app.ApplicationController;
import com.tiro_app.tiro_app.R;
import com.tiro_app.tiro_app.controller.HorlogeVIEW;
import com.tiro_app.tiro_app.controller.RowsPersonnalTimeline;

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


        if(aptInterface != null){
            holder.dataView= data.get(position);
            Integer timer =  holder.dataView.timer - aptInterface.getCurrentHorlog();
            holder.tv_username.setText(holder.dataView.username);
            holder.dataPositionItem = position;
            holder.tv_timer.setText(HorlogeVIEW.convertSecondeToReadable(timer));
            this.position = position;

            if(timer > 1) {
                holder.rlt_layout_hide.setVisibility(View.GONE);
                holder.Cptl_rlt_layout_data.setVisibility(View.VISIBLE);

            }else {
                holder.rlt_layout_hide.setVisibility(View.VISIBLE);
                holder.Cptl_rlt_layout_data.setVisibility(View.GONE);

            }

            if(holder.dataView.rawData.length() > 0 ){
                holder.tv_rawData.setText(Html.fromHtml(holder.dataView.rawData));
                holder.tv_rawData.setVisibility(View.VISIBLE);
            }else{
                holder.tv_rawData.setVisibility(View.GONE);
                Log.i("tv rawdata", "GONE");
            }

            if(timer < 60){
                holder.tv_timer.setTextColor(context.getResources().getColor(R.color.error_color));
            }else{
                holder.tv_timer.setTextColor(context.getResources().getColor(R.color.bluePolice));
            }

            if(holder.dataView.avatarUri !=null){
                holder.avatar.setImageUrl("http://tiro-app.com/user/avatar/" + holder.dataView.avatarUri, ApplicationController.getsInstance().getImageLoader());
            }else{
                holder.avatar.setDefaultImageResId(R.drawable.ic_image_camera_blueaction_no_picture);
            }

            if(holder.dataView.photoUri !=null){
                holder.Cptl_nImageView_photo.setVisibility(View.VISIBLE);
                holder.Cptl_nImageView_photo.setImageUrl("http://tiro-app.com/post/photo/" + holder.dataView.photoUri, ApplicationController.getsInstance().getImageLoader());
            }else{
                holder.Cptl_nImageView_photo.setVisibility(View.GONE);
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

        CardView rlt_layout_showing;
        RelativeLayout rlt_layout_hide;
        RelativeLayout Cptl_rlt_layout_data;

        TextView tv_username;
        TextView tv_rawData;
        TextView tv_timer;
        TextView tv_comment;
        NetworkImageView avatar, Cptl_nImageView_photo;
        Button editPost;
        RowsPersonnalTimeline dataView;
        Context context;
        public int dataPositionItem;


        public ViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();
            rlt_layout_showing  = (CardView) itemView.findViewById(R.id.Cptl_rlt_layout_showing);
            rlt_layout_hide = (RelativeLayout) itemView.findViewById(R.id.Cptl_rlt_layout_hide);
            Cptl_rlt_layout_data = (RelativeLayout) itemView.findViewById(R.id.Cptl_rlt_layout_data);
            tv_username = (TextView) itemView.findViewById(R.id.Cptl_textview_username);
            tv_rawData = (TextView) itemView.findViewById(R.id.Cptl_textview_rawData);
            tv_timer = (TextView) itemView.findViewById(R.id.Cptl_textview_timeLeft);
            Cptl_nImageView_photo = (NetworkImageView) itemView.findViewById(R.id.Cptl_nImageView_photo);
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
