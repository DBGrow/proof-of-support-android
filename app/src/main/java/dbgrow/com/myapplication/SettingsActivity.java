package dbgrow.com.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SettingsActivity extends AppCompatActivity {
    KeyUtils keyUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        keyUtils = new KeyUtils(this);

        Button public_button = findViewById(R.id.copy_pub);

        public_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyPublic();

                Toast toast = Toast.makeText(getApplicationContext(), "Copied Public Key!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        String ip_string =SupportHTTPClient.getIP(this);

        final EditText ip = findViewById(R.id.ip);
        if(ip_string!=null){
            ip.setText(ip_string);
        }

        Button button = findViewById(R.id.ip_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_address = ip.getText().toString();
                //set to permanent storage
                SupportHTTPClient.setIP(getApplicationContext(), ip_address);
            }
        });


    }

    String getIP() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.ip), null);
    }

    void setIP(String ip) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.ip), ip);
        editor.commit();
    }


    void copyPublic() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("dbgrow_public_key", keyUtils.getPublicKeyString());
            Log.i(getClass().getSimpleName(), keyUtils.getPublicKeyString());
            clipboard.setPrimaryClip(clip);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
