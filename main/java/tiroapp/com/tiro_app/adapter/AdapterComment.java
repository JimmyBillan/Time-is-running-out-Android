package tiroapp.com.tiro_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.RowsComment;

/**
 * Created by user on 18/08/2015.
 */
public class AdapterComment extends RecyclerView.Adapter<AdapterComment.customCommentsViewHolder>{
    private List<RowsComment> data;
    private int position;
    private Context context;

    public AdapterComment( List<RowsComment> data) {
        this.data = data;
    }

    public void refresh(List<RowsComment> data){
        this.data = data;
        notifyDataSetChanged();
    }



    @Override
    public customCommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_comment_row, parent, false);
        return new customCommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(customCommentsViewHolder holder, int position) {
        holder.dataview = data.get(position);
        this.position = position;
        holder.row_comment_textview_username.setText(holder.dataview.username);
        holder.row_comment_textview_thecomment.setText(Html.fromHtml(holder.dataview.comment));

        if(holder.dataview.avatar.equals("null")){
            holder.row_comment_niv_avatar.setDefaultImageResId(R.drawable.ic_account_circle_white_48dp);
        }else{
            holder.row_comment_niv_avatar.setImageUrl("http://tiro-app.com/user/avatar/" + holder.dataview.avatar, ApplicationController.getsInstance().getImageLoader());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class customCommentsViewHolder extends RecyclerView.ViewHolder{

        NetworkImageView row_comment_niv_avatar;
        TextView row_comment_textview_username;
        TextView row_comment_textview_thecomment;
        RowsComment dataview;


        public customCommentsViewHolder(View itemView) {
            super(itemView);
            row_comment_textview_username = (TextView) itemView.findViewById(R.id.row_comment_textview_username);
            row_comment_textview_thecomment = (TextView) itemView.findViewById(R.id.row_comment_textview_thecomment);
            row_comment_niv_avatar = (NetworkImageView) itemView.findViewById(R.id.row_comment_niv_avatar);

        }

    }

}
