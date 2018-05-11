package dbgrow.com.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import dbgrow.com.myapplication.datastructures.Checkin;


public class MainActivity extends AppCompatActivity {

    KeyUtils keyUtils;

    RecyclerView checkinRecycler;
    LinearLayoutManager layoutManager;
    CheckinRecyclerViewAdapter checkinRecyclerViewAdapter;

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


        //setup recyclerview and UI
        checkinRecycler = findViewById(R.id.checkin_recycler);
        layoutManager = new LinearLayoutManager(this);
        checkinRecycler.setLayoutManager(layoutManager);

        checkinRecyclerViewAdapter = new CheckinRecyclerViewAdapter(new ArrayList<Checkin>());
        checkinRecycler.setAdapter(checkinRecyclerViewAdapter);

        final EditText checkinMessage = findViewById(R.id.checkin_message);

        //buttons
        Button checkinButton = findViewById(R.id.checkin_button);
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = checkinMessage.getText().toString();
                if (message.length() == 0) {

                    Toast toast = Toast.makeText(getApplicationContext(), "Please type a message!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                new SupportHTTPClient(getApplicationContext()).commitCheckin(message, new OnCheckinCompleteListener() {
                    @Override
                    public void onSuccess(Checkin checkin) {
                        checkinRecyclerViewAdapter.addCheckin(checkin);
                    }

                    @Override
                    public void onFailure(int status, String body) {

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

        //check if we have keys, if not then generate
        ArrayList<String> fileList = new ArrayList<>(Arrays.asList(fileList()));

        if (!fileList.contains("private.pem")) {
            Log.i(getClass().getSimpleName(), "Did not contain private key! Generating...");
            keyUtils.generateKeys();
            Log.i(getClass().getSimpleName(), "Done!");
        } else {
//            Log.i(getClass().getSimpleName(), "Found Keys!");

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
        new SupportHTTPClient(this).getCheckins(new OnGetCheckinsCompleteListener() {
            @Override
            public void onSuccess(ArrayList<Checkin> checkins) throws UnsupportedEncodingException {
                Log.i(getClass().getSimpleName(), "Checkins success! " + checkins.size() + " checkins");
                checkinRecyclerViewAdapter.addCheckins(checkins);
            }

            @Override
            public void onFailure(int status, String body) {
                Log.i(getClass().getSimpleName(), "Get checkins fail");
            }
        });
    }
}
