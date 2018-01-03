package com.example.sumerpatel.chatapp.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.service.LocManager;
import com.example.sumerpatel.chatapp.utils.UserDetails;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Chat extends AppCompatActivity {

    final static String GROUP_KEY_GUEST = "group_key_guest";
    private static final int PHOTO_PICKER = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAPTURE_VIDEO_REQUEST_CODE = 3;
    static boolean currentlyRunning = false;
    Context context;
    LinearLayout layout;
    ImageView sendButton, emojiButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2, reference3;
    TextView time1, time2, txtMsg1, txtMsg2, location1, location2;
    ImageView uploadImage1, uploadImage2, messageStatus;
    Notification notification;
    Uri uri;
    long[] v = {500, 1000};
    int count, i;
    StorageReference storageRef, mountainsRef;
    FirebaseStorage storage;
    DatabaseReference databaseRef;
    StorageReference photoRef;
    NotificationManager notificationManager;
    Toolbar toolbar;
    Location currentLocation;
    TextCrawler textCrawler;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        layout = (LinearLayout) findViewById(R.id.layout1);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        emojiButton = (ImageView) findViewById(R.id.emojiButton);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("UserPosition"));
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatapplication-1cb5c.appspot.com");//.child("ic_launcher.png");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://chatapplication-1cb5c.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://chatapplication-1cb5c.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        //reference3 = new Firebase("https://chatapplication-1cb5c.firebaseio.com/messages/" + UserDetails.time);

        messageArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 500);

                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButtonClick("", "");
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                String time = map.get("time").toString();
                String deleteFlag = map.get("deleteFlag").toString();
                String imagePath = map.get("image").toString();
                String location = map.get("location").toString();
                String strKey = dataSnapshot.getKey();

                /*notification = new Notification(R.drawable.ic_launcher,
                        "A new notification", System.currentTimeMillis());

                notification.flags |= Notification.FLAG_NO_CLEAR
                        | Notification.FLAG_ONGOING_EVENT;*/

                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                if (userName.equals(UserDetails.username)) {
                    if (deleteFlag.equals("0")) {
                        addMessageBox("You:-\n" + message, 1, " " + time + " ", strKey, imagePath, location);
                    }
                } else {
                    //if (currentlyRunning == true) {
                    if (deleteFlag.equals("0")) {
                        if (userName.equals(UserDetails.chatWith) && currentlyRunning == true) {
                            notificationManager.cancelAll();
                            addMessageBox(UserDetails.chatWith + ":-\n" + message, 2, " " + time + " ", strKey, imagePath, location);
                        } else {
                            //if (!userName.equals(getIntent().getStringExtra("UserPosition"))) {
                            Intent intent = new Intent(Chat.this, MainActivity.class);
                            // use System.currentTimeMillis() to have a unique ID for the pending intent
                            PendingIntent pIntent = PendingIntent.getActivity(Chat.this, (int) System.currentTimeMillis(), intent, 0);
                            notification = new Notification.Builder(Chat.this)
                                    .setContentTitle(getIntent().getStringExtra("UserPosition"))
                                    .setContentText(message)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentIntent(pIntent)
                                    .setAutoCancel(true)
                                    .setStyle(new Notification.InboxStyle()
                                            .addLine(message))
                                    //.setSound(uri)
                                    //.setVibrate(v)
                                    .addAction(R.drawable.ic_smiles_car, "Call", pIntent)
                                    .addAction(R.drawable.ic_smiles_bell, "Read", pIntent).build();

                            /*NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);*/
                            if (count == 1) {
                                count++;
                            } else {
                                i++;
                            }
                            notification.number += i;
                            notificationManager.notify(0, notification);
                            // addMessageBox(UserDetails.chatWith + ":-\n" + message, 2, " " + time + " ", strKey);
                        }
                        // }
                    }
                }
            }
            //}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void sendButtonClick(String uri, String uriLocation) {
        String messageText = messageArea.getText().toString();

        //if (!messageText.equals("")) {
        messageArea.setText("");
        if (!uri.equals("") && uriLocation.equals("")) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", messageText);
            map.put("user", UserDetails.username);
            map.put("time", getCurrentSystemTime());
            map.put("deleteFlag", "0");
            map.put("image", String.valueOf(uri));
            map.put("location", "");
            reference1.push().setValue(map);
            reference2.push().setValue(map);
            // reference3.push().setValue(map);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", messageText);
            map.put("user", UserDetails.username);
            map.put("time", getCurrentSystemTime());
            map.put("deleteFlag", "0");
            map.put("image", "");
            map.put("location", uriLocation);
            reference1.push().setValue(map);
            reference2.push().setValue(map);
        }
        //}
    }

    @Override
    protected void onStart() {
        currentlyRunning = true;
        super.onStart();
    }

    @Override
    protected void onPause() {
        currentlyRunning = false;
        super.onPause();
    }

    private String getCurrentSystemTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.US);
        return format.format(calendar.getTime());
    }

    public void addMessageBox(String message, int type, String time, final String strKey, String imagePath, String location) {
        /*TextView textView = new TextView(Chat.this);
        textView.setText(time + message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);*/
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams layoutParamTime = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == 1) {
            final View view = getLayoutInflater().inflate(R.layout.chat_user_sender, null);
            txtMsg1 = (TextView) view.findViewById(R.id.textview_message);
            time1 = (TextView) view.findViewById(R.id.textview_time);
            location1 = (TextView) view.findViewById(R.id.textview_location);
            messageStatus = (ImageView) view.findViewById(R.id.user_send_status);
            txtMsg1.setMaxWidth(400);
            location1.setMaxWidth(400);
            txtMsg1.setPadding(8, 0, 8, 8);
            location1.setPadding(8, 0, 8, 8);
            uploadImage1 = (ImageView) view.findViewById(R.id.uploadImage1);
            txtMsg1.setText(message);
            time1.setText(time);

            /*if (ChatMessage.getMessageStatus() == Status.DELIVERED) {
                Toast.makeText(Chat.this, "Delivered", Toast.LENGTH_SHORT).show();
                messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.message_got_receipt_from_target));
            } else if (ChatMessage.getMessageStatus() == Status.SENT) {
                Toast.makeText(Chat.this, "Sent", Toast.LENGTH_SHORT).show();
                messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.message_got_receipt_from_server));
            }*/
            if (!imagePath.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(350, 350);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime.addRule(RelativeLayout.BELOW, R.id.uploadImage1);
                layoutParamTime.addRule(RelativeLayout.ALIGN_RIGHT);
                time1.setLayoutParams(layoutParamTime);

                //time1.setGravity(Gravity.END|Gravity.BOTTOM|Gravity.RIGHT);
                uploadImage1.setLayoutParams(layoutParams);
                Picasso.with(Chat.this).load(imagePath).into(uploadImage1);
                /*URL url = null;
                try {
                    url = new URL(imagePath);
                    Picasso.with(getBaseContext())
                            .load(String.valueOf(url))
                            .into(uploadImage2 );

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }*/
            }
            if (!location.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime.addRule(RelativeLayout.END_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.RIGHT_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                time1.setLayoutParams(layoutParamTime);
                location1.setLayoutParams(layoutParams);
                /*textCrawler
                        .makePreview(callback, location1.getText().toString());*/
                location1.setText(location);
            }
            layout.addView(view);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setMessage("Are you sure you want to delete");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Firebase taskRef = reference1.child(strKey);
                            System.out.println("===IF" + taskRef.toString());
                            Map<String, Object> taskMap = new HashMap<String, Object>();
                            taskMap.put("deleteFlag", "1");
                            taskRef.updateChildren(taskMap);
                            layout.removeView(v);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });

        } else {
            final View view1 = getLayoutInflater().inflate(R.layout.chat_user_receiver, null);
            txtMsg2 = (TextView) view1.findViewById(R.id.textview_message);
            time2 = (TextView) view1.findViewById(R.id.textview_time);
            uploadImage2 = (ImageView) view1.findViewById(R.id.uploadImage2);
            location2 = (TextView) view1.findViewById(R.id.textview_location);
            txtMsg2.setMaxWidth(400);
            location2.setMaxWidth(400);
            txtMsg2.setPadding(8, 0, 8, 8);
            txtMsg2.setText(message);
            time2.setText(time);
            if (!imagePath.equals("")) {
                Toast.makeText(this, "Image View 1", Toast.LENGTH_SHORT).show();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(350, 350);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                //layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParamTime.addRule(RelativeLayout.BELOW, R.id.uploadImage2);
                time1.setLayoutParams(layoutParamTime);
                uploadImage2.setLayoutParams(layoutParams);
                Picasso.with(Chat.this).load(imagePath).into(uploadImage2);
            }
            if (!location.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime.addRule(RelativeLayout.END_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.RIGHT_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                time2.setLayoutParams(layoutParamTime);
                location2.setLayoutParams(layoutParams);
                /*textCrawler
                        .makePreview(callback, location1.getText().toString());*/
                location2.setText(location);
            }
            layout.addView(view1);
            view1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    view1.setSelected(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setMessage("Are you sure you want to delete");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Firebase taskRef = reference1.child(strKey);
                            System.out.println("===ELSE" + taskRef.toString());
                            Map<String, Object> taskMap = new HashMap<String, Object>();
                            taskMap.put("deleteFlag", "1");
                            taskRef.updateChildren(taskMap);
                            layout.removeView(v);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }

        //layout.addView(textView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Recieved result from image picker
        if (requestCode == PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // Get a reference to the location where we'll store our photos
            storageRef = storage.getReference("chat_photos_" + getString(R.string.app_name));
            // Get a reference to store file at chat_photos/<FILENAME>
            photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] dataa = baos.toByteArray();
                //uploadImage.setImageBitmap(bitmap);
                final UploadTask uploadTask = photoRef.putBytes(dataa);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(Chat.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /*uploadImage1 = (ImageView) findViewById(R.id.uploadImage1);
                        uploadImage2 = (ImageView) findViewById(R.id.uploadImage2);*/
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        sendButtonClick(downloadUri.toString(), "");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // Get a reference to the location where we'll store our photos
            storageRef = storage.getReference("chat_photos_" + getString(R.string.app_name));
            // Get a reference to store file at chat_photos/<FILENAME>
            photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] dataa = baos.toByteArray();
                //uploadImage.setImageBitmap(bitmap);
                final UploadTask uploadTask = photoRef.putBytes(dataa);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(Chat.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        sendButtonClick(downloadUri.toString(), "");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {

        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Chat.this, Users.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.image_send:
                popUpShow();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void popUpShow() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);
        LinearLayout gallery, location, camera, video;
        final AlertDialog alertDialog = dialogBuilder.create();
        gallery = (LinearLayout) dialogView.findViewById(R.id.gallery);
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
        location = (LinearLayout) dialogView.findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                getLocation();
            }
        });
        camera = (LinearLayout) dialogView.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        video = (LinearLayout) dialogView.findViewById(R.id.video);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, CAPTURE_VIDEO_REQUEST_CODE);
            }
        });
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void getLocation() {

        final ProgressDialog progressDialog = new ProgressDialog(Chat.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final LocManager gps = new LocManager(Chat.this, this);
        // check if GPS enabled
        if (gps.canGetLocation) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    acquirelocation(gps);
                }
            }, 5000);
            // \n is for new line
            //geocoder = new Geocoder(this, Locale.getDefault());
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        progressDialog.dismiss();
    }

    private void acquirelocation(LocManager gps) {
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        Toast.makeText(Chat.this, "Your Location is - \nLat: "
                + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        //String uri = "http://maps.google.com/maps?saddr=" + currentLocation.getLatitude()+","+currentLocation.getLongitude();
        String uriLocation = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
        sendButtonClick("", uriLocation);
    }
}