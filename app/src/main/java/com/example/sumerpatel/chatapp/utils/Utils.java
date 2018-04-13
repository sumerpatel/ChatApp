package com.example.sumerpatel.chatapp.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sumerpatel.chatapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sumerpatel on 4/6/2018.
 */

public class Utils {

    /**
     * Method to Add or Replace fragment.
     *
     * @param fragment        Opening fragment
     * @param fragmentManager Support for Fragment to add/replace.
     * @param tag             Set tag to manager back stack through out application, ignore if tag is null
     * @param container       fragment container
     */
    public static void startFragment(final Fragment fragment,
                                     final FragmentManager fragmentManager,
                                     final String tag,
                                     final int container) {
        if (tag == null) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(container, fragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(container, fragment)
                    .addToBackStack(tag)
                    .commit();
        }
    }

    public static String getCurrentSystemTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.US);
        return format.format(calendar.getTime());
    }

    /**
     * Function to create progress dialog
     *
     * @param mContext Context of the activity that calls this function
     */
    public static ProgressDialog createProgressDialog(Context mContext) {
        return createProgressDialog(mContext, "", false);
    }

    /**
     * Function to create progress dialog with custom message
     *
     * @param mContext Context of the activity that calls this function
     * @param msg      Custom message to be displayed in progress dialog
     */
    public static ProgressDialog createProgressDialog(Context mContext, String msg) {
        return createProgressDialog(mContext, msg, false);
    }

    /**
     * Function to create progress dialog with custom message and custom progress drawable size
     *
     * @param mContext              Context of the activity that calls this function
     * @param msg                   Custom message to be displayed in progress dialog
     * @param showLargeSizeProgress Set custom progress drawable style if true.
     */
    public static ProgressDialog createProgressDialog(Context mContext, String msg, boolean showLargeSizeProgress) {

        ProgressDialog dialog;
        if (TextUtils.isEmpty(msg))
            dialog = new ProgressDialog(mContext);
        else
            dialog = new ProgressDialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        try {
            dialog.show();

            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View view = LayoutInflater.from(mContext).inflate(R.layout.progressdialog, null);
            if (!TextUtils.isEmpty(msg)) {
                view.findViewById(R.id.tvProgressMsg).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tvProgressMsg)).setText(msg);

                view.findViewById(R.id.rlProgressContainer).setBackgroundResource(R.color.colorPrimary50opacity);
            }

            if (showLargeSizeProgress) {
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
                progressBar.setScaleX(1.5f);
                progressBar.setScaleY(1.5f);
                progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            }

            dialog.setContentView(view);
        } catch (WindowManager.BadTokenException ignore) {

        }
        return dialog;
    }

    /**
     * Function to get URI for the image to be loaded
     *
     * @param context The context of the Calling Activity
     * @param inImage Bitmap for which URI is to be returned
     * @return The Image file object
     */
    public static File getImageUri(Activity context, Bitmap inImage) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut;
        File file = new File(path, "Photo" + timeStamp + ".jpg"); // the File to save to
        try {
            fOut = new FileOutputStream(file);
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close(); // do not forget to close the stream

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Function to validate the length of the input string in EditText
     *  @param input  Input EditText
     *
     */
    public static boolean validateLength(EditText input) {
        return input.getText().toString().trim().length() == 10;
    }

    /**
     * Function to validate if the value of any field in the list is empty
     *
     * @param fields List of fields to be verified
     */
    public static boolean validateEmptyFields(EditText fields) {
        //for (MyCustomEditText currentField : fields) {
            if (fields.getText().toString().length() <= 0) {
                return false;
            }
        //}
        return true;
    }

    /**
     * Function to check if the input Email is in valid format
     *
     * @param email The email to be validated
     */
    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
