package com.example.sumerpatel.chatapp.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.activity.MainActivity;
import com.example.sumerpatel.chatapp.activity.ProfileActivity;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.Utils;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private static final int PHOTO_PICKER = 0;
    private static final int CAMERA_REQUEST = 1;
    public Context context;
    public View view;

    public EditText edtName, edtEmailId, edtMobileNumber, edtPassword;
    public TextView btnRegister, btnLogin;
    public String strMobileNumber, strPassword;
    public ProgressDialog progressDialog;
    public ImageView ivSetProfilePic, ivProfilePic;
    public String strProfilePath = "";
    StorageReference storageRef, photoRef;
    FirebaseStorage storage;
    private ProgressBar picProgress;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        context = getActivity();
        init();
        return view;
    }

    private void init() {
        edtName = (EditText) view.findViewById(R.id.frag_register_et_fname);
        edtEmailId = (EditText) view.findViewById(R.id.frag_register_et_email);
        edtMobileNumber = (EditText) view.findViewById(R.id.frag_register_et_mobile);
        edtPassword = (EditText) view.findViewById(R.id.frag_register_et_password);
        btnRegister = (TextView) view.findViewById(R.id.registerButton);
        btnLogin = (TextView) view.findViewById(R.id.login);
        ivSetProfilePic = (ImageView) view.findViewById(R.id.frag_register_iv_change_pic);
        ivProfilePic = (ImageView) view.findViewById(R.id.frag_register_iv_profile_pic);
        picProgress = (ProgressBar) view.findViewById(R.id.frag_register_pb_profile_pic);

        Firebase.setAndroidContext(context);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatapp-f3ccb.appspot.com");

        String str = getString(R.string.click_here_to_login);
        String lastWord = str.substring(str.lastIndexOf(" ") + 1);
        Log.e("Login", lastWord);

        SpannableStringBuilder spannableString = new SpannableStringBuilder(getString(R.string.click_here_to_login));
        /*spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorClickables)),
                lastWord.charAt(0), lastWord.length(), 0);*/

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                widget.invalidate();
                Utils.startFragment(new LoginFragment(), getFragmentManager(), null, R.id.container_layout);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, str.indexOf(lastWord), str.indexOf(lastWord) + lastWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorClickables)),
                str.indexOf(lastWord), str.indexOf(lastWord) + lastWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        btnLogin.setHighlightColor(Color.TRANSPARENT);
        btnLogin.setText(spannableString);
        btnLogin.setMovementMethod(LinkMovementMethod.getInstance());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRegister();
            }
        });

        ivSetProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileUploadPopup();
            }
        });
    }

    private void showProfileUploadPopup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_PICKER && resultCode == RESULT_OK) {
            picProgress.setVisibility(View.VISIBLE);
            Uri selectedImageUri = data.getData();
            storageRef = storage.getReference("Profile_pic_" + Constants.username);

            if (selectedImageUri != null) {
                photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            }

            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
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
                            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), downloadProfilePic);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e("Profile", "Uri : " + downloadProfilePic);
                        //loadImage(downloadProfilePic.toString());
                        Picasso.with(context).load(downloadProfilePic).into(ivProfilePic);
                        strProfilePath = downloadProfilePic.toString();
                        picProgress.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onClickRegister() {
        final String strName = edtName.getText().toString();
        final String strEmailId = edtEmailId.getText().toString();
        strMobileNumber = edtMobileNumber.getText().toString();
        strPassword = edtPassword.getText().toString();

        if (isValidated()) {
            mDisplayProgressDialog(true);
            String url = "https://chatapp-f3ccb.firebaseio.com/users.json";

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Firebase reference = new Firebase("https://chatapp-f3ccb.firebaseio.com/users");

                    Map<String, String> map = new HashMap<String, String>();

                    if (s.equals("null")) {
                        /*map.put("password", strPassword);
                        map.put("mobile", strMobileNumber);
                        map.put("emailId", strEmailId);
                        map.put("name", strName);
                        map.put("profilePic", strProfilePath);
                        reference.push().setValue(map);*/
                        reference.child(strMobileNumber).child("password").setValue(strPassword);
                        reference.child(strMobileNumber).child("mobile").setValue(strMobileNumber);
                        reference.child(strMobileNumber).child("emailId").setValue(strEmailId);
                        reference.child(strMobileNumber).child("name").setValue(strName);
                        reference.child(strMobileNumber).child("profilePic").setValue(strProfilePath);
                        Toast.makeText(context, "registration successful", Toast.LENGTH_LONG).show();
                        Utils.startFragment(new LoginFragment(), getFragmentManager(), null, R.id.container_layout);
                    } else {
                        try {
                            JSONObject obj = new JSONObject(s);

                            if (!obj.has(strMobileNumber)) {
                                reference.child(strMobileNumber).child("password").setValue(strPassword);
                                reference.child(strMobileNumber).child("mobile").setValue(strMobileNumber);
                                reference.child(strMobileNumber).child("emailId").setValue(strEmailId);
                                reference.child(strMobileNumber).child("name").setValue(strName);
                                reference.child(strMobileNumber).child("profilePic").setValue(strProfilePath);
                                Toast.makeText(context, "registration successful", Toast.LENGTH_LONG).show();
                                Utils.startFragment(new LoginFragment(), getFragmentManager(), null, R.id.container_layout);
                            } else {
                                Toast.makeText(context, "username already exists", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    mDisplayProgressDialog(false);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("Register", "Error : " + volleyError);
                    mDisplayProgressDialog(false);
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(context);
            rQueue.add(request);
        }
    }

    private boolean isValidated() {

        if (edtName.getText().toString().trim().length() <= 0) {
            edtName.setError("can't be blank");
            return false;
        } else if (!Utils.isValidEmail(edtEmailId.getText().toString())) {
            edtName.setError("Please provide valid email address");
            return false;
        } else if (!edtName.getText().toString().matches("[\\^a-zA-Z ]+")) {
            edtName.setError("Please provide valid Name");
            return false;
        } else if (!Utils.validateLength(edtMobileNumber)) {
            edtName.setError("Please provide valid Mobile Number");
            return false;
        } else if (strPassword.length() < 5) {
            edtName.setError("at least 5 characters long");
            return false;
        }
        return true;
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
