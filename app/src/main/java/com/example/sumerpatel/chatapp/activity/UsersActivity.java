package com.example.sumerpatel.chatapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sumerpatel.chatapp.Manifest;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.adapter.AdapterUsers;
import com.example.sumerpatel.chatapp.interfaces.RecyclerViewClickListener;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.SharedPrefs;
import com.example.sumerpatel.chatapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class UsersActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public Activity context;
    RecyclerView usersList;
    TextView noUsersText;
    ArrayList<String> arrayList = new ArrayList<>();
    AdapterUsers adapterUsers;
    int totalUsers = 0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        context = UsersActivity.this;
        setupActionbar();

        usersList = (RecyclerView) findViewById(R.id.usersList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        usersList.setLayoutManager(mLayoutManager);
        noUsersText = (TextView) findViewById(R.id.noUsersText);

        /*progressDialog = new ProgressDialog(UsersActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();*/
        mDisplayProgressDialog(true);

        String url = "https://chatapp-f3ccb.firebaseio.com/users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
        rQueue.add(request);

        try {
           /* if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);*/
            int location = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int rdstorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int wrtstorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (location != PackageManager.PERMISSION_GRANTED || rdstorage != PackageManager.PERMISSION_GRANTED || wrtstorage != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setting up topmost header view with dynamic header title.
     */
    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TextView) toolbar.findViewById(R.id.toolbar_txt_title)).setText(getString(R.string.app_name));
        ((TextView) toolbar.findViewById(R.id.toolbar_txt_title)).setTextColor(ContextCompat.getColor(this, R.color.white_with70pacity));
        ImageView leftIcon = (ImageView) toolbar.findViewById(R.id.action_bar_iv_left);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void doOnSuccess(String s) {
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while (i.hasNext()) {
                key = i.next().toString();

                Log.e("Users", "Keys : " + key);

                SharedPrefs.getInstance(context).saveName(key, obj.getJSONObject(key).getString("name"));

                if (!key.equals(Constants.username)) {
                    arrayList.add(key);
                }

                totalUsers++;
            }

            adapterUsers = new AdapterUsers(UsersActivity.this, arrayList, obj, new RecyclerViewClickListener() {
                @Override
                public void onClick(View view, int position) {
                    Constants.chatWith = arrayList.get(position);
                    Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
                    intent.putExtra("UserPosition", Constants.chatWith);
                    startActivity(intent);
                    finish();
                }
            });

            if (totalUsers <= 1) {
                noUsersText.setVisibility(View.VISIBLE);
                usersList.setVisibility(View.GONE);
            } else {
                noUsersText.setVisibility(View.GONE);
                usersList.setVisibility(View.VISIBLE);
                //usersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList));
                usersList.setAdapter(adapterUsers);
            }

            //progressDialog.dismiss();
            mDisplayProgressDialog(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(UsersActivity.this, MainActivity.class));
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                SharedPrefs.getInstance(context).clearAppPrefs();
                Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.profile:
                Intent intentProfile = new Intent(UsersActivity.this, ProfileActivity.class);
                startActivity(intentProfile);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
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