package com.example.sumerpatel.chatapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.example.sumerpatel.chatapp.utils.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Users extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
//    ListView usersList;
    RecyclerView usersList;
    TextView noUsersText;
    ArrayList<String> arrayList = new ArrayList<>();
    AdapterUsers adapterUsers;
    int totalUsers = 0;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

//        usersList = (ListView) findViewById(R.id.usersList);
        usersList = (RecyclerView) findViewById(R.id.usersList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        usersList.setLayoutManager(mLayoutManager);
        noUsersText = (TextView) findViewById(R.id.noUsersText);

        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.show();

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

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);

        /*usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserDetails.chatWith = arrayList.get(position);
                Intent intent = new Intent(Users.this, Chat.class);
                intent.putExtra("UserPosition", UserDetails.chatWith);
                startActivity(intent);
            }
        });*/

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

    public void doOnSuccess(String s) {
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while (i.hasNext()) {
                key = i.next().toString();

                if (!key.equals(UserDetails.username)) {
                    arrayList.add(key);
                }

                totalUsers++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterUsers = new AdapterUsers(Users.this, arrayList, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                UserDetails.chatWith = arrayList.get(position);
                Intent intent = new Intent(Users.this, Chat.class);
                intent.putExtra("UserPosition", UserDetails.chatWith);
                startActivity(intent);
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

        pd.dismiss();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Users.this, MainActivity.class));
        finish();
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sign_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                Intent intent = new Intent(Users.this, MainActivity.class);
                startActivity(intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}