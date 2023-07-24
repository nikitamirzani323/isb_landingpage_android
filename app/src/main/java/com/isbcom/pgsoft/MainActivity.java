package com.isbcom.pgsoft;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progress;
    private RequestQueue queue;
    private String version,path,token_db,domain_db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);
        version = getResources().getString(R.string.version);
        path = getResources().getString(R.string.path_api);
        pref = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
        editor = pref.edit();
        Log.d("version",version);
        Log.d("path_api",path);
        _checkLoginServer("https://api.isbapps.com/api/init","asd");
    }
    public void _checkLoginServer(final String URLSERVER, String client_hostname){
        progress = ProgressDialog.show(MainActivity.this, "", "Connect to server", true);
        StringRequest stringRequestLogin = new StringRequest(Request.Method.POST, URLSERVER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progress.dismiss();
                        /* showMessage("info",response,"ok");*/
                        try {
                            JSONObject datalogin = new JSONObject(response);
                            Log.d("token",datalogin.getString("token"));
                            token_db = datalogin.getString("token");

                            if(token_db.equals("")) {
                                Thread timerThread = new Thread() {
                                    public void run() {
                                        try {
                                            sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } finally {
                                            editor.putString("domain", domain_db);
                                            editor.apply();
                                            finish();
                                            Intent intent = new Intent(getApplicationContext(), Home.class);
                                            startActivity(intent);
                                        }
                                    }
                                };
                                timerThread.start();
                            }else{
                                Thread timerThread = new Thread(){
                                    public void run(){
                                        try{
                                            sleep(2000);
                                        }catch(InterruptedException e){
                                            e.printStackTrace();
                                        }finally{
                                            finish();
                                            Intent intent = new Intent(getApplicationContext(),Website.class);
                                            startActivity(intent);
                                        }
                                    }
                                };
                                timerThread.start();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            Toasty.error(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.cancel();
                handleVolleyError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {         // Menambahkan parameters post
                Map<String, String> params = new HashMap<String, String>();
//                params.put("username", PHONE);
                params.put("client_hostname", client_hostname);
                params.put("typedevice", "phone");
                return params;
            }
        };
        stringRequestLogin.setRetryPolicy(
                new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, // 2500
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 1
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); //1f
        queue.add(stringRequestLogin);
    }
    public void message(Context context, String title, String msg, String type){
        AlertDialog.Builder Checkbuilder = new AlertDialog.Builder(context);
        Checkbuilder.setTitle(title);
        Checkbuilder.setMessage(msg);

        switch(type){
            case "1"://INTERNET CONNECTION
                Checkbuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });break;
            case "2"://TIMEOUT CONNECTION
                Checkbuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
                break;
            case "3"://UPDATE VERSION
                Checkbuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.isb.tv"));
                        startActivity(intent);
                    }
                });break;
            case "4"://BLOCKED
                Checkbuilder.setPositiveButton("Administrator", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });break;
        }
        AlertDialog alertDialog = Checkbuilder.create();
        alertDialog.show();

    }
    private void handleVolleyError(VolleyError error) {
        if (error instanceof TimeoutError) {
            message(MainActivity.this, "No Internet", "Connection Time Out", "2");
        } else if (error instanceof NoConnectionError) {
            message(MainActivity.this, "No Internet","Please check your device's connection settings","1");
        } else if (error instanceof AuthFailureError) {
            Toasty.error(MainActivity.this, "The server unreachable", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ServerError) {
            Toasty.error(MainActivity.this, "The server trouble", Toast.LENGTH_SHORT).show();
        } else if (error instanceof NetworkError) {
            Toasty.error(MainActivity.this, "Connection Internet Trouble", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ParseError) {
            Toasty.error(MainActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
        }
    }
}