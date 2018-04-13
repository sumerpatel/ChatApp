package com.example.sumerpatel.chatapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.SharedPrefs;
import com.example.sumerpatel.chatapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private Activity context;
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = SplashActivity.this;

        //final SharedPreferences prefs = getSharedPreferences("MyPrefs", 0);
        boolean isUserSignedUp = SharedPrefs.getInstance(context).getUserSignedIn();
        Log.e("SplashScreen", "First Run : " + isUserSignedUp);
        if (!isUserSignedUp) {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            //String url = "https://chatapplication-1cb5c.firebaseio.com/users.json";
            String url = "https://chatapp-f3ccb.firebaseio.com/users.json";
            /*final ProgressDialog progressDialog = new ProgressDialog(SplashActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();*/
            mDisplayProgressDialog(true);

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    try {
                        JSONObject obj = new JSONObject(s);

                        Log.e("SplashScreen", "Username : " + SharedPrefs.getInstance(context).getUsername());
                        Log.e("SplashScreen", "Password : " + SharedPrefs.getInstance(context).getPassword());

                        if (!obj.has(SharedPrefs.getInstance(context).getUsername())) {
                            Toast.makeText(SplashActivity.this, "user not found", Toast.LENGTH_LONG).show();
                        } else if (obj.getJSONObject(SharedPrefs.getInstance(context).getUsername()).getString("password").
                                equals(SharedPrefs.getInstance(context).getPassword())) {
                            Constants.username = SharedPrefs.getInstance(context).getUsername();
                            Constants.password = SharedPrefs.getInstance(context).getPassword();
                            startActivity(new Intent(SplashActivity.this, UsersActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SplashActivity.this, "incorrect password", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //progressDialog.dismiss();
                    mDisplayProgressDialog(false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("" + volleyError);
                    //progressDialog.dismiss();
                    mDisplayProgressDialog(false);
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(SplashActivity.this);
            rQueue.add(request);
        }
    }

    /**
     * This method is used to hide/show progress dialog
     *
     * @param show True to show progress, False to hide progress
     */
    private void mDisplayProgressDialog(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = Utils.createProgressDialog(context);
                progressDialog.show();
            } else {
                progressDialog.show();
            }
        } else {
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
}
