package dbgrow.com.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import dbgrow.com.myapplication.datastructures.Checkin;

public class CheckinService extends Service {

    // Create the Handler object (on the main thread by default)
    final Handler handler = new Handler();
    //    final long REQUIRED_ENTRY_INTERVAL_MS = 180 * 60000; //3 hrs
    final long REQUIRED_ENTRY_INTERVAL_MS = 30000; //30 sec
    final long ALERT_INTERVAL_MS = 5 * 60000; //5 minutes
    static final String CHANNEL_ID = "9038429384";
    static final int NOTIFICATION_ID = 12312221;
    long lastAlert = -1;
    boolean posted = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName(), "The Checkin Service is live!");
        if (!posted) {
            Log.i(getClass().getSimpleName(), "STARTING SERVICE HANDLER");
            createNotificationChannel();
            handler.post(checkinTask); //start the checkin timer if it's not already
            posted = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }

    private Runnable checkinTask = new Runnable() {
        @Override
        public void run() {
            Log.d(getClass().getSimpleName(), "Called on main thread");
            //enumerate the checkins, see how long it's been since the last one!

            new SupportHTTPClient(getApplicationContext()).getCheckins(new OnGetCheckinsCompleteListener() {
                @Override
                public void onSuccess(ArrayList<Checkin> checkins) throws UnsupportedEncodingException {
                    if (checkins.size() == 0) {
                        Log.i(getClass().getSimpleName(), "SERVICE NO CHECKINS");
//                        Toast toast = Toast.makeText(getApplicationContext(), "NO CHECKINS", Toast.LENGTH_SHORT);
//                        toast.show();
                        return; //uninitialized support chain!
                    }

                    Date latestEntry = new Date(checkins.get(checkins.size() - 1).timestamp);

//                    Toast toast = Toast.makeText(getApplicationContext(), "DELTA " + (new Date().getTime() - latestEntry.getTime()), Toast.LENGTH_SHORT);
//                    toast.show();

                    if (new Date().getTime() - latestEntry.getTime() > REQUIRED_ENTRY_INTERVAL_MS) {

                        Log.i(getClass().getSimpleName(), "OVER ENTRY MS");
//                        toast = Toast.makeText(getApplicationContext(), "OVER ENTRY", Toast.LENGTH_SHORT);
//                        toast.show();

                        //send an alert if we haven't already
                        if (new Date().getTime() - lastAlert > ALERT_INTERVAL_MS) { //send the notification

//                            toast = Toast.makeText(getApplicationContext(), "NOTIFICATION", Toast.LENGTH_SHORT);
//                            toast.show();

                            Log.i(getClass().getSimpleName(), "SENDING A NOTIFICATION!!!");
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setContentTitle("DBGrow POS")
                                    .setContentText("It's been a long time since someone has checked in!")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                        }
                    }
                }

                @Override
                public void onFailure(int status, String body) {
                    Log.e(getClass().getSimpleName(), "Error retrieving checkins in service");
                }
            });

            handler.postDelayed(this, 10000);
        }
    };

    private void sendAlert() {
        lastAlert = new Date().getTime();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DBGrowPOS";
            String description = "We up brah";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}