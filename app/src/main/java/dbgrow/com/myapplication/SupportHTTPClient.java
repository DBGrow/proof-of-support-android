package dbgrow.com.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import dbgrow.com.myapplication.datastructures.Checkin;

interface OnGetNonceCompleteListener {
    void onSuccess(String nonce) throws UnsupportedEncodingException;

    void onFailure(int status, String body);
}

interface OnGetCheckinsCompleteListener {
    void onSuccess(ArrayList<Checkin> ckeckins) throws UnsupportedEncodingException;

    void onFailure(int status, String body);
}

interface OnCheckinCompleteListener {
    void onSuccess(Checkin checkin);

    void onFailure(int status, String body);
}

public class SupportHTTPClient {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Context context;
    private String host;
    private KeyUtils keyUtils;

    SupportHTTPClient(Context ctx) {
        keyUtils = new KeyUtils(ctx);
        context = ctx;
        host = getIP(context);
    }

    static String getIP(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("ip", null);
    }

    static boolean setIP(Context context, String ip) {

        if (!Util.validateIP(ip)) return false;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ip", ip);
        editor.commit();
        return true;
    }

    public void getNonce(final OnGetNonceCompleteListener listener) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setLoggingEnabled(false);
        Log.i(getClass().getSimpleName(), "Getting nonce...");
        client.get("http://" + host + ":3000/nonce", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i(getClass().getSimpleName(), "Got Nonce!!! :" + new String(response));
                try {
                    listener.onSuccess(new String(response));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(getClass().getSimpleName(), "Got nonce failure: " + statusCode);
                listener.onFailure(statusCode, "");
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void getCheckins(final OnGetCheckinsCompleteListener listener) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setLoggingEnabled(false);
//        Log.i(getClass().getSimpleName(), "Getting checkins...");
        client.get("http://" + host + ":3000/checkins", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i(getClass().getSimpleName(), "Got Checkins!!! :" + new String(response));
                try {
                    Type listType = new TypeToken<List<Checkin>>() {
                    }.getType();
                    ArrayList<Checkin> checkins = gson.fromJson(new String(response), listType);

                    listener.onSuccess(checkins);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.i(getClass().getSimpleName(), "Got Checkins failure: " + statusCode);
                listener.onFailure(statusCode, "");
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.i(getClass().getSimpleName(), "Attempting request retry...");
            }
        });
    }

    public void commitCheckin(final String message, final OnCheckinCompleteListener listener) {

        getNonce(new OnGetNonceCompleteListener() {
            @Override
            public void onSuccess(String nonce) throws UnsupportedEncodingException {

                Log.i(getClass().getSimpleName(), "Got nonce for checkin: " + nonce);

                AsyncHttpClient client = new AsyncHttpClient();
                client.setLoggingEnabled(false);
                String signed_nonce;
                try {
                    signed_nonce = keyUtils.signToHexString(nonce);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("nonce", nonce);
                params.put("signed_nonce", signed_nonce);
                params.put("message", message);

                client.post("http://" + host + ":3000/checkin?nonce=" + URLEncoder.encode(nonce, "utf-8") + "&signed_nonce=" + URLEncoder.encode(signed_nonce, "utf-8") + "&message=" + URLEncoder.encode(message), params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        Log.i(getClass().getSimpleName(), "Got post checkin success: " + statusCode);
                        listener.onSuccess(gson.fromJson(new String(response), Checkin.class));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        listener.onFailure(statusCode, "");
                        Log.e(getClass().getSimpleName(), "Got post checkin FAILURE: " + statusCode);
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });
            }

            @Override
            public void onFailure(int status, String body) {
                listener.onFailure(status, body);
            }
        });

    }
}

