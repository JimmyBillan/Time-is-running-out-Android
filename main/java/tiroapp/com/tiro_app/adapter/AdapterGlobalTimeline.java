package tiroapp.com.tiro_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import tiroapp.com.tiro_app.controller.Horloge;
import tiroapp.com.tiro_app.controller.RowsGlobalTimeline;
import tiroapp.com.tiro_app.controller.RowsPersonnalTimeline;

/**
 * Created by user on 06/08/2015.
 */
public class AdapterGlobalTimeline  extends RecyclerView.Adapter<AdapterGlobalTimeline.ViewHolder> {

    List<RowsGlobalTimeline> data = Collections.emptyList();
    Integer position ;
    Context context ;

    public AdapterGlobalTimeline(List<RowsGlobalTimeline> dataset) {
        this.data = dataset;
    }

    public void refresh(List<RowsGlobalTimeline> data)
    {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public AdapterGlobalTimeline.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.custom_global_timeline_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterGlobalTimeline.ViewHolder holder, int position) {
        holder.dataView= data.get(position);
        this.position = position;
        holder.tv_username.setText(holder.dataView.username);
        holder.dataPositionItem = position;
        holder.tv_rawData.setText(holder.dataView.rawData);
        holder.tv_timer.setText(Horloge.convertSecondeToReadable(holder.dataView.timer - holder.dataView.dateNow));
        QuerysetAvatar(holder.dataView.username, holder);
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
                    Log.i("uri", cachedData.getString("profilPicUri"));
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

    public class ViewHolder extends RecyclerView.ViewHolder  {


        TextView tv_username;
        TextView tv_rawData;
        TextView tv_timer;
        NetworkImageView avatar;

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
        }
    }

    public interface AgtInterface{
        int getCurrentHorlog();
    }
}
