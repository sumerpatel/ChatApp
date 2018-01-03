package com.example.sumerpatel.chatapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.utils.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.sumerpatel.chatapp.activity.MainActivity.PASSWORD;
import static com.example.sumerpatel.chatapp.activity.MainActivity.USERNAME;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final SharedPreferences settings = getSharedPreferences("MyPrefs", 0);
        boolean firstRun = settings.getBoolean("FirstRun", false);
        if (firstRun == false) {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            String url = "https://chatapplication-1cb5c.firebaseio.com/users.json";
            final ProgressDialog pd = new ProgressDialog(SplashActivity.this);
            pd.setMessage("Loading...");
            pd.show();

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    try {
                        JSONObject obj = new JSONObject(s);

                        if (!obj.has(settings.getString("Username", USERNAME))) {
                            Toast.makeText(SplashActivity.this, "user not found", Toast.LENGTH_LONG).show();
                        } else if (obj.getJSONObject(settings.getString("Username", USERNAME)).getString("password").
                                equals(settings.getString("Password", PASSWORD))) {
                            UserDetails.username = settings.getString("Username", USERNAME);
                            UserDetails.password = settings.getString("password", PASSWORD);
                            startActivity(new Intent(SplashActivity.this, Users.class));
                        } else {
                            Toast.makeText(SplashActivity.this, "incorrect password", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    pd.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("" + volleyError);
                    pd.dismiss();
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(SplashActivity.this);
            rQueue.add(request);
        }
    }
}
