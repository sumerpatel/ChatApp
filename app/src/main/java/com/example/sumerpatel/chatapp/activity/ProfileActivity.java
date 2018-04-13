package com.example.sumerpatel.chatapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.SharedPrefs;
import com.example.sumerpatel.chatapp.utils.Utils;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PHOTO_PICKER = 0;
    private static final int CAMERA_REQUEST = 1;
    private ImageView ivProfilePic;

    public Context context;

    StorageReference storageRef;
    FirebaseStorage storage;
    StorageReference photoRef;
    private ProgressBar picProgress;
    private ProgressDialog progressDialog;

    private TextView tvUsername, tvEmailId, tvMobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = ProfileActivity.this;
//        String strUsername = getIntent().getStringExtra("user_chat_with");
//        TextView txtUsername = (TextView) findViewById(R.id.user_name);
//        txtUsername.setText(String.format("%s%s", strUsername.substring(0, 1).toUpperCase(), strUsername.substring(1).toLowerCase()));

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatapp-f3ccb.appspot.com");

        setupActionbar();
        init();

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

        RequestQueue rQueue = Volley.newRequestQueue(ProfileActivity.this);
        rQueue.add(request);


        ImageView ivEditProfilePic = (ImageView) findViewById(R.id.act_profile_iv_change_pic);
        ivEditProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPopup();
            }
        });
    }

    private void init() {
        ivProfilePic = (ImageView) findViewById(R.id.act_profile_pic);
        picProgress = (ProgressBar) findViewById(R.id.act_profile_pb_pic);
        tvUsername = (TextView) findViewById(R.id.act_profile_user_name);
        tvEmailId = (TextView) findViewById(R.id.act_profile_email_id);
        tvMobileNumber = (TextView) findViewById(R.id.act_profile_mobile_number);
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

    private void doOnSuccess(String response) {
        try {
            JSONObject obj = new JSONObject(response);

            String profileUrl = obj.getJSONObject(Constants.username).getString("profilePic");

            Log.e("Profile", "Username: " + Constants.username);
            Log.e("Profile", "Profile path : " + profileUrl);

            if (profileUrl != null && !profileUrl.equalsIgnoreCase("")) {
                Picasso.with(context).load(profileUrl).into(ivProfilePic);
            }

            tvUsername.setText(obj.getJSONObject(Constants.username).getString("name"));
            tvEmailId.setText(obj.getJSONObject(Constants.username).getString("emailId"));
            tvMobileNumber.setText(obj.getJSONObject(Constants.username).getString("mobile"));

            mDisplayProgressDialog(false);
        }catch (Exception e){
            e.printStackTrace();
            mDisplayProgressDialog(false);
        }
    }

    private void showEditPopup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_profile_pic, null);
        dialogBuilder.setView(dialogView);
        LinearLayout gallery, camera, removeImage;
        final AlertDialog alertDialog = dialogBuilder.create();

        gallery = (LinearLayout) dialogView.findViewById(R.id.layout_gallery);
        camera = (LinearLayout) dialogView.findViewById(R.id.layout_camera);
        removeImage = (LinearLayout) dialogView.findViewById(R.id.layout_remove);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "success"), PHOTO_PICKER);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_PICKER && resultCode == RESULT_OK) {
            picProgress.setVisibility(View.VISIBLE);
            Uri selectedImageUri = data.getData();
            storageRef = storage.getReference("Profile_pic_" + Constants.username);

            if (selectedImageUri != null) {
                photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            }

            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageData = baos.toByteArray();
                final UploadTask uploadTask = photoRef.putBytes(imageData);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadProfilePic = taskSnapshot.getDownloadUrl();
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(ProfileActivity.this.getContentResolver(), downloadProfilePic);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e("Profile", "Uri : " + downloadProfilePic);
                        uploadImageToUsersData(downloadProfilePic.toString());
                        Picasso.with(context).load(downloadProfilePic).into(ivProfilePic);
                        picProgress.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToUsersData(String strUrl) {
        Firebase reference = new Firebase("https://chatapp-f3ccb.firebaseio.com/users");
        reference.child(Constants.username).child("profilePic").setValue(strUrl);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
