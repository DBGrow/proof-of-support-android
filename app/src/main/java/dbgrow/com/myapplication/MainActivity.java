package dbgrow.com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import dbgrow.com.myapplication.datastructures.Checkin;


public class MainActivity extends AppCompatActivity {

    KeyUtils keyUtils;
    RecyclerView checkinRecycler;
    LinearLayoutManager layoutManager;
    CheckinRecyclerViewAdapter checkinRecyclerViewAdapter;
    RelativeLayout getCheckinProgress;
    ProgressBar submitCheckinProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        keyUtils = new KeyUtils(this);

        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        getCheckinProgress = findViewById(R.id.checkin_progress);
        submitCheckinProgress = findViewById(R.id.checkin_submit_progress);

        //setup recyclerview and UI
        checkinRecycler = findViewById(R.id.checkin_recycler);
        layoutManager = new LinearLayoutManager(this);
//        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        checkinRecycler.setLayoutManager(layoutManager);
/*

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        checkinRecycler.addItemDecoration(dividerItemDecoration);
*/

        checkinRecyclerViewAdapter = new CheckinRecyclerViewAdapter(new ArrayList<Checkin>());
        checkinRecycler.setAdapter(checkinRecyclerViewAdapter);

        final EditText checkinMessage = findViewById(R.id.checkin_message);

        //buttons
        final Button checkinButton = findViewById(R.id.checkin_button);
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = checkinMessage.getText().toString();
                if (message.length() == 0) {

                    Toast toast = Toast.makeText(getApplicationContext(), "Please type a message!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                checkinButton.setText("");
                submitCheckinProgress.setVisibility(View.VISIBLE);

                new SupportHTTPClient(getApplicationContext()).commitCheckin(message, new OnCheckinCompleteListener() {
                    @Override
                    public void onSuccess(Checkin checkin) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Checkin Success!", Toast.LENGTH_SHORT);
                        toast.show();

                        Log.i(getClass().getSimpleName(), new Gson().toJson(checkin));
                        checkinRecyclerViewAdapter.addCheckin(checkin);
                        checkinRecycler.smoothScrollToPosition(checkinRecyclerViewAdapter.getItemCount() - 1);

                        checkinButton.setText("Checkin");
                        checkinMessage.setText(""); //clear text
                        submitCheckinProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int status, String body) {
                        checkinButton.setText("Checkin");
                        submitCheckinProgress.setVisibility(View.GONE);
                        Toast toast = Toast.makeText(getApplicationContext(), "Failed to submit checkin! : " + status, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

        if (SupportHTTPClient.getIP(this) == null) {
            Toast toast = Toast.makeText(this, "Must set IP before making requests", Toast.LENGTH_SHORT);
            toast.show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        //check if west have keys, if not then generate
        ArrayList<String> fileList = new ArrayList<>(Arrays.asList(fileList()));

        if (!fileList.contains("private.pem")) {
            Log.i(getClass().getSimpleName(), "Did not contain private key! Generating...");
            keyUtils.generateKeys();
            Log.i(getClass().getSimpleName(), "Done!");
        }

        try {
            String signed = keyUtils.signToHexString("IM REAL COOL!");
//            Log.i(getClass().getSimpleName(), "SIGNED:\n" + signed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start services
        Intent intent = new Intent(this, CheckinService.class);
        startService(intent);

        checkinRecyclerViewAdapter.clearCheckins();
        getCheckinProgress.setVisibility(View.VISIBLE);
        new SupportHTTPClient(this).getCheckins(new OnGetCheckinsCompleteListener() {
            @Override
            public void onSuccess(ArrayList<Checkin> checkins) throws UnsupportedEncodingException {
                Log.i(getClass().getSimpleName(), "Checkins success! " + checkins.size() + " checkins");
                checkinRecyclerViewAdapter.addCheckins(checkins);
                getCheckinProgress.setVisibility(View.GONE);

                //force scroll to bottom
                checkinRecycler.smoothScrollToPosition(checkinRecyclerViewAdapter.getItemCount() - 1);

                if (checkins.size() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "No checkins to show!", Toast.LENGTH_SHORT);
                    toast.show();
                    //show the no results icon

                    return;
                }
            }

            @Override
            public void onFailure(int status, String body) {
                Log.i(getClass().getSimpleName(), "Get checkins fail");
                getCheckinProgress.setVisibility(View.GONE);
                Toast toast = Toast.makeText(getApplicationContext(), "Failed to get Checkins!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
