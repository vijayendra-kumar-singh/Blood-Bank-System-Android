package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        tx.setText("Login");
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        Button login = (Button) findViewById(R.id.buttonLogin);
        final EditText number = (EditText) findViewById(R.id.editTextNumber);
        final EditText password = (EditText) findViewById(R.id.editTextPassword);
        TextView forgot = (TextView) findViewById(R.id.forgot);
        CheckBox show = (CheckBox) findViewById(R.id.show);

        show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(null);
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.number, null, true);
                final EditText number = alertLayout.findViewById(R.id.num);
                final Button cancel = alertLayout.findViewById(R.id.cancel);
                final Button ok = alertLayout.findViewById(R.id.ok);

                AlertDialog.Builder alert = new AlertDialog.Builder(login.this);
                alert.setView(alertLayout);
                alert.setCancelable(false);
                final AlertDialog dialog = alert.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                number.setMaxEms(10);

                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String num = number.getText().toString();
                        if (num.length() != 10) {
                            Toast.makeText(login.this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            checkNumber(num);
                        }
                    }
                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = number.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (num.length() != 10) {
                    Toast.makeText(login.this, "Invalid number", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.length() < 6) {
                        Toast.makeText(login.this, "Password must have at least 6 digits", Toast.LENGTH_SHORT).show();
                    } else {
                        signin(num, pass);
                    }
                }
            }
        });
    }

    private void signin(final String number, String pass) {
        final ProgressDialog progressDialog = ProgressDialog.show(login.this, null, "Signing in...", false, false);
        RequestParams params = new RequestParams();
        params.put("number", number);
        params.put("password", pass);
        String url = "https://blood-help-india.herokuapp.com/login.php";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------success------", "---------success------" + String.valueOf(response));
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        SharedPreferences sharedPreferences = login.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("number", number);
                        editor.putString("utype", response.getString("utype"));
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(), availableRequests.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        change(intent);
                    } else {
                        Toast.makeText(login.this, response.getString("status"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(login.this, "network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void checkNumber(final String num) {
        final ProgressDialog progressDialog = ProgressDialog.show(login.this, null, "Checking...", false, false);
        RequestParams params = new RequestParams();
        params.put("number", num);
        String url = "https://blood-help-india.herokuapp.com/forgototp.php";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------success------", "---------success------" + String.valueOf(response));
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        verify(num, response.getString("otp"));
                    } else {
                        Toast.makeText(login.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(login.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verify(final String num, final String Otp) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.otp, null, true);
        final EditText number = alertLayout.findViewById(R.id.num);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(login.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        number.setMaxEms(6);

        number.setText(Otp);

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
                abort(num);
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = number.getText().toString();
                if (otp.length() != 6) {
                    Toast.makeText(login.this, "Enter correct OTP", Toast.LENGTH_SHORT).show();
                } else {
                    final ProgressDialog progressDialog = ProgressDialog.show(login.this, null, "Verifying OTP...", false, false);

                    RequestParams params = new RequestParams();
                    params.put("otp", otp);
                    params.put("number", num);
                    String url = "https://blood-help-india.herokuapp.com/forgot.php";

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(url, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            dialog.dismiss();
                            progressDialog.dismiss();
                            Log.i("---------success------", "---------success------" + String.valueOf(response));
                            try {
                                if (Objects.equals(response.getString("status"), "success")) {
                                    Toast.makeText(login.this, "New password is " + response.getString("result"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(login.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                            progressDialog.dismiss();
                            Log.i("---------fail----------", "---------fail------" + response);
                            Toast.makeText(login.this, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void abort(String num) {
        final ProgressDialog progressDialog = ProgressDialog.show(login.this, null, "Rolling back changes...", false, false);

        RequestParams params = new RequestParams();
        params.put("number", num);
        String url = "https://blood-help-india.herokuapp.com/abort.php";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------success------", "---------success------" + String.valueOf(response));
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        Toast.makeText(login.this, "Process aborted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(login.this, "Server error", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(login.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void change(final Intent intent){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, 1);
    }

}
