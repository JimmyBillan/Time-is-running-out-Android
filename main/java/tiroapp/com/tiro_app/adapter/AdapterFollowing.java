package tiroapp.com.tiro_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.RowsFollowing;

/**
 * Created by user on 31/07/2015.
 */
public class AdapterFollowing extends RecyclerView.Adapter<AdapterFollowing.ViewHolder> {

    List<RowsFollowing> data = Collections.emptyList();
    Integer position ;
    Context context ;

    public AdapterFollowing(List<RowsFollowing> data) {
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

    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        TextView tv_nb_follower;

        RowsFollowing dataView;

        public ViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            tv_username = (TextView) itemView.findViewById(R.id.row_following_contact_textview_username);
            tv_nb_follower = (TextView) itemView.findViewById(R.id.row_following_contact_textview_nbFollower);
        }
    }
}
