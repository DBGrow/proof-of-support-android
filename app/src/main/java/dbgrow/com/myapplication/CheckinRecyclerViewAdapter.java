package dbgrow.com.myapplication;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
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

    public void clearCheckins() {
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Checkin checkin = checkins.get(position);
        TextView message = holder.view.findViewById(R.id.message);
        if (checkin.message != null) {
            message.setText(checkin.message);
        }

        TextView name = holder.view.findViewById(R.id.name);
        if (checkin.signer != null) {
            name.setText(checkin.signer);
        } else {
            name.setText("[unknown]");
        }

        TextView timestamp = holder.view.findViewById(R.id.timestamp);
        if (checkin.timestamp != 0L) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(checkin.timestamp));

            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);


            timestamp.setText(month + "/" + day + " " + hour + ":" + minute);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkin.timestamp != 0L) {
                    Toast toast = Toast.makeText(holder.view.getContext(), prettyTime.format(new Date(checkin.timestamp)), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return checkins.size();
    }
}
