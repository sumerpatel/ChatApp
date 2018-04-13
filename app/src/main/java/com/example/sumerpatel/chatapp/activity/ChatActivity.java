package com.example.sumerpatel.chatapp.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.service.LocManager;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.SharedPrefs;
import com.example.sumerpatel.chatapp.utils.Utils;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int PHOTO_PICKER = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAPTURE_VIDEO_REQUEST_CODE = 3;
    private static final int AUDIO_REQUEST_CODE = 4;
    private static final int RESULT_PICK_CONTACT = 5;
    static boolean currentlyRunning = false;
    Context context;
    LinearLayout layout;
    ImageView sendButton, emojiButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    TextView txtTimeSender, txtTimeReceiver, txtMsgSender, txtMsgReceiver, locationSender, locationReceiver, tvTitle;
    ImageView ivSender, ivReceiver, messageStatus, ivPlayAudio, ivPauseAudio;
    SeekBar seekBarAudio;
    MediaPlayer mediaPlayer;
    Notification notification;
    Uri uri;
    long[] v = {500, 1000};
    int count, i, mediaPos, mediaMax;
    String titleName = "";
    StorageReference storageRef, mountainsRef;
    FirebaseStorage storage;
    StorageReference photoRef;
    NotificationManager notificationManager;
    Toolbar toolbar;
    Handler handler = new Handler();
    private ImageView ivBackButton;
    private LinearLayout layoutProfile;

    /**
     * The Move seek bar. Thread to move seekbar based on the current position
     * of the song
     */
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {

                int mediaPos_new = mediaPlayer.getCurrentPosition();
                int mediaMax_new = mediaPlayer.getDuration();
                seekBarAudio.setMax(mediaMax_new);
                seekBarAudio.setProgress(mediaPos_new);

                handler.postDelayed(this, 100); //Looping the thread after 0.1 second
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = ChatActivity.this;

        init();
        setuptActionBar();
        setupListerners();

        mediaPlayer = new MediaPlayer();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatapp-f3ccb.appspot.com");//.child("ic_launcher.png");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://chatapp-f3ccb.firebaseio.com/messages/" + Constants.username + "_" + Constants.chatWith);
        reference2 = new Firebase("https://chatapp-f3ccb.firebaseio.com/messages/" + Constants.chatWith + "_" + Constants.username);
        //reference3 = new Firebase("https://chatapplication-1cb5c.firebaseio.com/messages/" + Constants.time);

        /*messageArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 100);

                return false;
            }
        });*/

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
                String video = map.get("video").toString();
                String audio = map.get("audio").toString();
                String contact = map.get("contact").toString();
                String strKey = dataSnapshot.getKey();

                /*notification = new Notification(R.drawable.ic_launcher,
                        "A new notification", System.currentTimeMillis());

                notification.flags |= Notification.FLAG_NO_CLEAR
                        | Notification.FLAG_ONGOING_EVENT;*/

                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                if (userName.equals(Constants.username)) {
                    if (deleteFlag.equals("0")) {
                        addMessageBox(/*"You:-\n" + */message, 1, " " + time + " ", strKey, imagePath,
                                location, video, audio, contact);
                    }
                } else {
                    //if (currentlyRunning == true) {
                    if (deleteFlag.equals("0")) {
                        if (userName.equals(Constants.chatWith) && currentlyRunning) {
                            notificationManager.cancelAll();
                            //addMessageBox(Constants.chatWith.substring(0, 1).toUpperCase() + Constants.chatWith.substring(1).toLowerCase() + ":-\n" + message, 2, " " + time + " ", strKey, imagePath, location, video, audio);
                            addMessageBox(/*titleName + ":-\n" + */message, 2, " " + time + " ", strKey, imagePath,
                                    location, video, audio, contact);
                        } else {
                            //if (!userName.equals(getIntent().getStringExtra("UserPosition"))) {
                            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                            // use System.currentTimeMillis() to have a unique ID for the pending intent
                            PendingIntent pIntent = PendingIntent.getActivity(ChatActivity.this, (int) System.currentTimeMillis(), intent, 0);
                            notification = new Notification.Builder(ChatActivity.this)
                                    //.setContentTitle(getIntent().getStringExtra("UserPosition"))
                                    .setContentTitle(titleName)
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
                            // addMessageBox(Constants.chatWith + ":-\n" + message, 2, " " + time + " ", strKey);
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

    private void init() {
        layout = (LinearLayout) findViewById(R.id.layout1);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        emojiButton = (ImageView) findViewById(R.id.emojiButton);
        layoutProfile = (LinearLayout) findViewById(R.id.layout_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.toolbar_title);
        ivBackButton = (ImageView) findViewById(R.id.toolbar_back_icon);
    }

    private void setuptActionBar() {
        setSupportActionBar(toolbar);
        String strChatUser = getIntent().getStringExtra("UserPosition");
        titleName = SharedPrefs.getInstance(context).getName(strChatUser);
        titleName = titleName.substring(0, 1).toUpperCase() + titleName.substring(1).toLowerCase();
        tvTitle.setText(titleName);

    }

    private void setupListerners() {

        messageArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 50);
            }
        });

        ivBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                profileIntent.putExtra("user_chat_with", titleName);
                startActivity(profileIntent);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageArea.getText().toString().trim().equalsIgnoreCase("")) {
                    sendButtonClick("", "", "", "", "");
                }
            }
        });
    }

    public void sendButtonClick(String uriImage, String uriLocation, String contact, String video, String audio) {
        String messageText = messageArea.getText().toString();

        messageArea.setText("");
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", messageText);
        map.put("user", Constants.username);
        map.put("time", Utils.getCurrentSystemTime());
        map.put("deleteFlag", "0");
        if (!uriImage.equals("") && uriLocation.equals("") && contact.equals("") && video.equals("") && audio.equals("")) {
            map.put("image", String.valueOf(uriImage));
            map.put("location", "");
            map.put("contact", "");
            map.put("video", "");
            map.put("audio", "");
        } else if (uriImage.equals("") && !uriLocation.equals("") && contact.equals("") && video.equals("") && audio.equals("")) {
            map.put("image", "");
            map.put("location", uriLocation);
            map.put("contact", "");
            map.put("video", "");
            map.put("audio", "");
        } else if (uriImage.equals("") && uriLocation.equals("") && !contact.equals("") && video.equals("") && audio.equals("")) {
            map.put("image", "");
            map.put("location", "");
            map.put("contact", contact);
            map.put("video", "");
            map.put("audio", "");
        } else if (uriImage.equals("") && uriLocation.equals("") && contact.equals("") && !video.equals("") && audio.equals("")) {
            map.put("image", "");
            map.put("location", "");
            map.put("contact", "");
            map.put("video", video);
            map.put("audio", "");
        } else if (uriImage.equals("") && uriLocation.equals("") && contact.equals("") && video.equals("") && !audio.equals("")) {
            map.put("image", "");
            map.put("location", "");
            map.put("contact", "");
            map.put("video", "");
            map.put("audio", audio);
        } else {
            map.put("image", "");
            map.put("location", "");
            map.put("contact", "");
            map.put("video", "");
            map.put("audio", "");
        }
        reference1.push().setValue(map);
        reference2.push().setValue(map);
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

    public void addMessageBox(String message, int type, String time, final String strKey, String imagePath, String location,
                              String video, String audio, String contact) {

        RelativeLayout.LayoutParams layoutParamTime = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == 1) {
            @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.chat_user_sender, null);
            txtMsgSender = (TextView) view.findViewById(R.id.textview_message);
            txtTimeSender = (TextView) view.findViewById(R.id.textview_time);
            locationSender = (TextView) view.findViewById(R.id.textview_location);
            messageStatus = (ImageView) view.findViewById(R.id.user_send_status);
            ivPlayAudio = (ImageView) view.findViewById(R.id.play_audio);
            ivPauseAudio = (ImageView) view.findViewById(R.id.pause_audio);
            seekBarAudio = (SeekBar) view.findViewById(R.id.audio_play_seekbar);
            LinearLayout audioLayout = view.findViewById(R.id.audio_layout);
            txtMsgSender.setMaxWidth(400);
            locationSender.setMaxWidth(400);
            txtMsgSender.setPadding(8, 0, 8, 8);
            locationSender.setPadding(8, 0, 8, 8);
            ivSender = (ImageView) view.findViewById(R.id.share_image);
            if (!contact.equalsIgnoreCase("")) {
                txtMsgSender.setText(contact);
            } else {
                txtMsgSender.setText(message);
            }
            txtTimeSender.setText(time);
            ivPlayAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.start();
                    ivPlayAudio.setVisibility(View.GONE);
                    ivPauseAudio.setVisibility(View.VISIBLE);
                }
            });
            ivPauseAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.pause();
                    ivPauseAudio.setVisibility(View.GONE);
                    ivPlayAudio.setVisibility(View.VISIBLE);
                }
            });

            /*if (ChatMessage.getMessageStatus() == Status.DELIVERED) {
                Toast.makeText(ChatActivity.this, "Delivered", Toast.LENGTH_SHORT).show();
                messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.message_got_receipt_from_target));
            } else if (ChatMessage.getMessageStatus() == Status.SENT) {
                Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.message_got_receipt_from_server));
            }*/
            if (!imagePath.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(350, 350);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime.addRule(RelativeLayout.BELOW, R.id.share_image);
                layoutParamTime.addRule(RelativeLayout.ALIGN_RIGHT);
                txtTimeSender.setLayoutParams(layoutParamTime);

                //txtTimeSender.setGravity(Gravity.END|Gravity.BOTTOM|Gravity.RIGHT);
                ivSender.setLayoutParams(layoutParams);
                Picasso.with(ChatActivity.this).load(imagePath).into(ivSender);
                /*URL url = null;
                try {
                    url = new URL(imagePath);
                    Picasso.with(getBaseContext())
                            .load(String.valueOf(url))
                            .into(ivReceiver );

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
                txtTimeSender.setLayoutParams(layoutParamTime);
                locationSender.setLayoutParams(layoutParams);
                /*textCrawler
                        .makePreview(callback, locationSender.getText().toString());*/
                locationSender.setText(location);
            }
            if (!audio.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 24, 0, 0);
                layoutParamTime.addRule(RelativeLayout.BELOW, R.id.audio_layout);
                layoutParamTime.addRule(RelativeLayout.ALIGN_RIGHT);
                layoutParamTime.addRule(RelativeLayout.ALIGN_END);
                //layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                txtTimeSender.setLayoutParams(layoutParamTime);
                audioLayout.setVisibility(View.VISIBLE);
                audioLayout.setLayoutParams(layoutParams);
                mediaPos = mediaPlayer.getCurrentPosition();
                mediaMax = mediaPlayer.getDuration();

                seekBarAudio.setMax(mediaMax); // Set the Maximum range of the
                seekBarAudio.setProgress(mediaPos);// set current progress to song's

                handler.removeCallbacks(moveSeekBarThread);
                handler.postDelayed(moveSeekBarThread, 100); //cal the thread after 100 milliseconds
            }
            if (!video.equalsIgnoreCase("")) {

            }
            layout.addView(view);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    builder.setMessage("Are you sure you want to delete");
                    builder.setPositiveButton(Html.fromHtml("<font color = '#000000'>" + "Delete" + "</font>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Firebase taskRef = reference1.child(strKey);
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
            txtMsgReceiver = (TextView) view1.findViewById(R.id.textview_message);
            txtTimeReceiver = (TextView) view1.findViewById(R.id.textview_time);
            ivReceiver = (ImageView) view1.findViewById(R.id.uploadImage2);
            locationReceiver = (TextView) view1.findViewById(R.id.textview_location);
            txtMsgReceiver.setMaxWidth(400);
            locationReceiver.setMaxWidth(400);
            txtMsgReceiver.setPadding(8, 0, 8, 8);
            txtMsgReceiver.setText(message);
            txtTimeReceiver.setText(time);
            if (!imagePath.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(350, 350);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                //layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParamTime.addRule(RelativeLayout.BELOW, R.id.uploadImage2);
                txtTimeReceiver.setLayoutParams(layoutParamTime);
                ivReceiver.setLayoutParams(layoutParams);
                Picasso.with(ChatActivity.this).load(imagePath).into(ivReceiver);
            }
            if (!location.equals("")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 48, 0, 0);
                layoutParamTime.addRule(RelativeLayout.END_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.RIGHT_OF, R.id.textview_location);
                layoutParamTime.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                txtTimeReceiver.setLayoutParams(layoutParamTime);
                locationReceiver.setLayoutParams(layoutParams);
                /*textCrawler
                        .makePreview(callback, locationSender.getText().toString());*/
                locationReceiver.setText(location);
            }
            layout.addView(view1);
            view1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    view1.setSelected(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    builder.setMessage("Are you sure you want to delete");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Firebase taskRef = reference1.child(strKey);
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
            if (selectedImageUri != null) {
                photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            }
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
                        Toast.makeText(ChatActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /*ivSender = (ImageView) findViewById(R.id.ivSender);
                        ivReceiver = (ImageView) findViewById(R.id.ivReceiver);*/
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        sendButtonClick(downloadUri.toString(), "", "", "", "");
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
            if (selectedImageUri != null) {
                photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            }
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
                        Toast.makeText(ChatActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        sendButtonClick(downloadUri.toString(), "", "", "", "");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                final UploadTask uploadTask = photoRef.putFile(uri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri videoUri = taskSnapshot.getDownloadUrl();
                        if (videoUri != null) {
                            sendButtonClick("", "", "", videoUri.toString(), "");
                        }
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri audioFile = data.getData();
            storageRef = storage.getReference("chat_audio_" + getString(R.string.app_name));
            try {
                if (audioFile != null) {
                    photoRef = storageRef.child(audioFile.getLastPathSegment());
                    final UploadTask uploadTask = photoRef.putFile(audioFile);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri audioUri = taskSnapshot.getDownloadUrl();
                            if (audioUri != null) {
                                sendButtonClick("", "", "", "", audioUri.toString());
                            }
                        }
                    });
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == RESULT_PICK_CONTACT && resultCode == RESULT_OK) {
            pickedContact(data);
        }
    }

    @SuppressLint("Recycle")
    private void pickedContact(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = "";
            String name = "";
            int phoneIndex = 0, nameIndex = 0;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            if (uri != null) {
                cursor = getContentResolver().query(uri, null, null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                phoneNo = cursor.getString(phoneIndex);
                name = cursor.getString(nameIndex);

                String strContact = name + "\n" + phoneNo;
                sendButtonClick("", "", strContact, "", "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ChatActivity.this, UsersActivity.class));
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
        LinearLayout gallery, location, camera, video, audio, contact;
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
        audio = (LinearLayout) dialogView.findViewById(R.id.audio);
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, AUDIO_REQUEST_CODE);
            }
        });
        contact = (LinearLayout) dialogView.findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void getLocation() {

        final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final LocManager gps = new LocManager(ChatActivity.this, this);
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
        Toast.makeText(ChatActivity.this, "Your Location is - \nLat: "
                + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        //String uri = "http://maps.google.com/maps?saddr=" + currentLocation.getLatitude()+","+currentLocation.getLongitude();
        String uriLocation = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
        sendButtonClick("", uriLocation, "", "", "");
    }
}