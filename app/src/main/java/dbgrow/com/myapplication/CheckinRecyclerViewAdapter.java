package dbgrow.com.myapplication;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

import dbgrow.com.myapplication.datastructures.Checkin;

public class CheckinRecyclerViewAdapter extends RecyclerView.Adapter<CheckinRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Checkin> checkins = new ArrayList<>();
    private PrettyTime prettyTime = new PrettyTime();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CheckinRecyclerViewAdapter(ArrayList<Checkin> initCheckins) {
        if (initCheckins != null) {
            checkins.addAll(initCheckins);
            notifyDataSetChanged();
        }
    }

    public void addCheckins(ArrayList<Checkin> checkins) {
        if (checkins != null) {
            this.checkins.addAll(checkins);
            notifyItemInserted(this.checkins.size() - 1);
        }
    }

    public void addCheckin(Checkin checkin) {
        if (checkin != null) {
            checkins.add(checkin);
            notifyItemInserted(checkins.size() - 1);
        }
    }

    public void clearCheckins(){
        checkins.clear();
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CheckinRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkin_template, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Checkin checkin = checkins.get(position);
        TextView message = holder.view.findViewById(R.id.message);
        if (checkin.message != null) {
            message.setText(checkin.message);
        }

        TextView name = holder.view.findViewById(R.id.name);
        if (checkin.signer != null) {
            name.setText(checkin.signer);
        }


        TextView timestamp = holder.view.findViewById(R.id.timestamp);
        if (checkin.timestamp != 0L) {
            Date date = new Date(checkin.timestamp);
            timestamp.setText(date.getHours() + ":" + (date.getMinutes()>9?date.getMinutes():"0"+date.getMinutes()));
            Log.i("Times", "" + System.currentTimeMillis());
            Log.i("Time", "" + checkin.timestamp);
            Log.i("Time", "" + new Date(checkin.timestamp).toLocaleString());
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return checkins.size();
    }
}
