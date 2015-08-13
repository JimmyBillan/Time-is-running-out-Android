package tiroapp.com.tiro_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.controller.RowsSearch;

/**
 * Created by user on 29/07/2015.
 */
public class AdapterSearchAddContact extends RecyclerView.Adapter<AdapterSearchAddContact.customSearchViewHolder>{
    private  List<RowsSearch> data;
    private int position;
    private Context context;
    private ClickListenner clickListenner;

    public AdapterSearchAddContact( List<RowsSearch> data) {
        this.data = data;
    }

    public void refresh(List<RowsSearch> data){
        this.data = data;
        notifyDataSetChanged();
    }



    @Override
    public customSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_search_contact_row, parent, false);
        return new customSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(customSearchViewHolder holder, int position) {
        holder.dataview = data.get(position);
        this.position = position;
        holder.tv_username.setText(holder.dataview.username);
        String nbfollower = Integer.toString(holder.dataview.nbFollower);
        holder.tv_nbFollower.setText(nbfollower+" followers");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class customSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv_username;
        TextView tv_nbFollower;
        ImageButton btn_addcontact;
        RowsSearch dataview;

        public customSearchViewHolder(View itemView) {
            super(itemView);
            tv_username = (TextView) itemView.findViewById(R.id.row_search_contact_textview_username);
            tv_nbFollower = (TextView) itemView.findViewById(R.id.row_search_contact_textview_nbFollower);
            btn_addcontact = (ImageButton) itemView.findViewById(R.id.row_search_contact_ImageButton_addContact);

            btn_addcontact.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if(clickListenner != null){
                clickListenner.addContact(v, getAdapterPosition());
            }
        }
    }

    public void setListenner(ClickListenner clickListenner){
        this.clickListenner = clickListenner;
    }

    public interface ClickListenner{
        public void addContact(View view, int position);
    }

}
