package tiroapp.com.tiro_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.Collections;
import java.util.List;

import tiroapp.com.tiro_app.ApplicationController;
import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.RowsFollowing;

/**
 * Created by user on 31/07/2015.
 */
public class AdapterFollow extends RecyclerView.Adapter<AdapterFollow.ViewHolder> {

    List<RowsFollowing> data = Collections.emptyList();
    Integer position ;
    Context context ;

    public AdapterFollow(List<RowsFollowing> data) {
       this.data = data;
    }

    public void refresh(List<RowsFollowing> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_following_contact_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.dataView = data.get(position);
        this.position = position;

        holder.tv_username.setText(holder.dataView.username);
        String nb_follower_conc = holder.dataView.nbFollower+" followers";
        holder.tv_nb_follower.setText(nb_follower_conc);
        if(!holder.dataView.profilPicUri.equals("null")){
            holder.Image_niv_list_following.setImageUrl("http://tiro-app.com/user/avatar/" + holder.dataView.profilPicUri, ApplicationController.getsInstance().getImageLoader());
        }


    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        TextView tv_nb_follower;
        NetworkImageView Image_niv_list_following;

        RowsFollowing dataView;

        public ViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            tv_username = (TextView) itemView.findViewById(R.id.row_following_contact_textview_username);
            tv_nb_follower = (TextView) itemView.findViewById(R.id.row_following_contact_textview_nbFollower);
            Image_niv_list_following = (NetworkImageView) itemView.findViewById(R.id.Image_niv_list_following);
            Image_niv_list_following.setDefaultImageResId(R.drawable.ic_image_camera_blueaction_no_picture);
        }
    }
}
