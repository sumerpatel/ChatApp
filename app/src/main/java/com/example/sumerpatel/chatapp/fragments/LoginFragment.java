package com.example.sumerpatel.chatapp.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.activity.UsersActivity;
import com.example.sumerpatel.chatapp.utils.Constants;
import com.example.sumerpatel.chatapp.utils.SharedPrefs;
import com.example.sumerpatel.chatapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String FIRSTRUN = "FirstRun";
    public View view;
    String strUsername, strPassword;
    SharedPreferences sharedpreferences;
    private Context context;
    private TextView tvRegister, tvLogin;
    private EditText edtUserMobileNumber, edtPassword;
    private ProgressDialog progressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
        context = getActivity();
        init();
        return view;
    }

    private void init() {
        tvRegister = (TextView) view.findViewById(R.id.frag_login_register);
        edtUserMobileNumber = (EditText) view.findViewById(R.id.frag_login_mobile_number);
        edtPassword = (EditText) view.findViewById(R.id.frag_login_password);
        tvLogin = (TextView) view.findViewById(R.id.frag_login_loginButton);

        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String str = getString(R.string.click_here_to_register);
        String lastWord = str.substring(str.lastIndexOf(" ") + 1);
        Log.e("Login", lastWord);

        SpannableStringBuilder spannableString = new SpannableStringBuilder(getString(R.string.click_here_to_register));
        /*spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorClickables)),
                lastWord.charAt(0), lastWord.length(), 0);*/

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                widget.invalidate();
                Utils.startFragment(new RegisterFragment(), getFragmentManager(), null, R.id.container_layout);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, str.indexOf(lastWord), str.indexOf(lastWord) + lastWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorClickables)),
                str.indexOf(lastWord), str.indexOf(lastWord) + lastWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegister.setHighlightColor(Color.TRANSPARENT);
        tvRegister.setText(spannableString);
        tvRegister.setMovementMethod(LinkMovementMethod.getInstance());

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick();
            }
        });
    }

    private void onLoginClick() {
        strUsername = edtUserMobileNumber.getText().toString();
        strPassword = edtPassword.getText().toString();
        /*final SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USERNAME, strMobileNumber);
        editor.putString(PASSWORD, strPassword);
        editor.putBoolean(FIRSTRUN, true);
        editor.apply();*/

        if (strUsername.equals("")) {
            edtUserMobileNumber.setError("can't be blank");
        } else if (strPassword.equals("")) {
            edtPassword.setError("can't be blank");
        } else {
            String url = "https://chatapp-f3ccb.firebaseio.com/users.json";
            mDisplayProgressDialog(true);

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    if (s.equals("null")) {
                        Toast.makeText(context, "Mobile Number not found", Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            JSONObject obj = new JSONObject(s);

                            if (!obj.has(strUsername)) {
                                Toast.makeText(context, "Username not found", Toast.LENGTH_LONG).show();
                            } else if (obj.getJSONObject(strUsername).getString("password").equals(strPassword)) {
                                Constants.username = strUsername;
                                Constants.password = strPassword;
                                SharedPrefs.getInstance(context).saveUsername(strUsername);
                                SharedPrefs.getInstance(context).savePassword(strPassword);
                                SharedPrefs.getInstance(context).saveUserSignedIn(true);

                                Log.e("Login", "Name : " + SharedPrefs.getInstance(context).getName(strUsername));

                                startActivity(new Intent(context, UsersActivity.class));
                                getActivity().finish();
                            } else {
                                Toast.makeText(context, "incorrect password", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("MainActivity", "Exception");
                            e.printStackTrace();
                        }
                    }
                    //pd.dismiss();
                    mDisplayProgressDialog(false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("MainActivity", "Error : " + volleyError);
                    //pd.dismiss();
                    mDisplayProgressDialog(false);
                }
            });

            RequestQueue rQueue = Volley.newRequestQueue(context);
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
