package com.example.mohan.bbms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        Button r = (Button) findViewById(R.id.r);
        Button l = (Button) findViewById(R.id.l);
        Button n = (Button) findViewById(R.id.nearby);
        Button search = (Button) findViewById(R.id.search);
        
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.number, null, true);
                final EditText number = alertLayout.findViewById(R.id.num);
                final Button cancel = alertLayout.findViewById(R.id.cancel);
                final Button ok = alertLayout.findViewById(R.id.ok);

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
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
                            Toast.makeText(MainActivity.this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            checkNumber(num);
                        }
                    }
                });
            }
        });

        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change(new Intent(MainActivity.this, login.class));
            }
        });
        
        n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nearby();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBankClick();
            }
        });
    }

    private void searchBankClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.banksearch, null, true);

        final EditText filter = alertLayout.findViewById(R.id.filter);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        Spinner filter_type = alertLayout.findViewById(R.id.filtertype);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        filter.setVisibility(GONE);

        List<String> groups = new ArrayList<>();
        groups.add("select");
        groups.add("Pin");
        groups.add("City");
        groups.add("Dist");

        final String f[] = new String[1];

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter_type.setAdapter(dataAdapter);

        filter_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filter.setVisibility(View.VISIBLE);
                if (i == 1) {
                    f[0] = "pincode";
                    filter.setMaxEms(6);
                    filter.setHint("Pincode");
                    filter.setInputType(InputType.TYPE_CLASS_NUMBER);
                    filter.setMaxLines(1);
                    filter.setText(null);
                    setEditTextMaxLength(filter, 6);
                } else if (i == 2) {
                    filter.setText(null);
                    f[0] = "city";
                    filter.setMaxEms(25);
                    filter.setHint("City name");
                    filter.setInputType(InputType.TYPE_CLASS_TEXT);
                    filter.setMaxLines(1);
                    setEditTextMaxLength(filter, 25);
                } else if (i == 3) {
                    f[0] = "district";
                    filter.setText(null);
                    filter.setMaxEms(25);
                    filter.setHint("District name");
                    filter.setInputType(InputType.TYPE_CLASS_TEXT);
                    filter.setMaxLines(1);
                    setEditTextMaxLength(filter, 25);
                } else {
                    f[0] = "None";
                    filter.setHint("Make a selection");
                    setEditTextMaxLength(filter, 0);
                }
            }

            void setEditTextMaxLength(EditText editText, int length) {
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(length);
                editText.setFilters(FilterArray);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                filter.setVisibility(GONE);
                f[0] = "None";
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Objects.equals(f[0], "None")) {
                    String fil = filter.getText().toString().trim();
                    if (Objects.equals(f[0], "pincode") && fil.length() != 6) {
                        Toast.makeText(getBaseContext(), "Invalid pincode", Toast.LENGTH_SHORT).show();
                    } else {
                        if (fil.length() < 3) {
                            Toast.makeText(getBaseContext(), "Invalid " + f[0] + " name", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            RequestParams params = new RequestParams();
                            params.put("filter", f[0]);
                            params.put("value", filter.getText().toString().trim().toLowerCase());

                            String url = "https://blood-help-india.herokuapp.com/searchbloodbanks.php";

                            String message = "Searching in database ...";

                            final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, null, message, false, false);

                            AsyncHttpClient client = new AsyncHttpClient();

                            client.get(url, params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                    progressDialog.dismiss();

                                    try {
                                        if (Objects.equals(response.getString("status"), "success")) {

                                            Intent i = new Intent(MainActivity.this, searchbanks.class);
                                            i.putExtra("data", response.toString());
                                            change(i);

                                        } else {
                                            Toast.makeText(getBaseContext(), response.getString("result"), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {

                                    progressDialog.dismiss();

                                    Toast.makeText(getBaseContext(), "network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Enter some data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void checkNumber(final String num) {
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, null, "Checking...", false, false);
        RequestParams params = new RequestParams();
        params.put("number", num);
        String url = "https://blood-help-india.herokuapp.com/otp.php";
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
                        Toast.makeText(MainActivity.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verify(final String num, final String Otp) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.otp, null, true);
        final EditText number = alertLayout.findViewById(R.id.num);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
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
                    Toast.makeText(MainActivity.this, "Enter correct OTP", Toast.LENGTH_SHORT).show();
                } else {
                    final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, null, "Verifying OTP...", false, false);

                    RequestParams params = new RequestParams();
                    params.put("otp", otp);
                    params.put("number", num);
                    String url = "https://blood-help-india.herokuapp.com/otpverify.php";

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(url, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            dialog.dismiss();
                            progressDialog.dismiss();
                            Log.i("---------success------", "---------success------" + String.valueOf(response));
                            try {
                                if (Objects.equals(response.getString("status"), "success")) {
                                    Intent i = new Intent(getBaseContext(), register.class);
                                    i.putExtra("number", num);
                                    change(i);
                                } else {
                                    Toast.makeText(MainActivity.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                            progressDialog.dismiss();
                            Log.i("---------fail----------", "---------fail------" + response);
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void abort(String num) {
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, null, "Rolling back changes...", false, false);

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
                        Toast.makeText(MainActivity.this, "Process aborted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void nearby(){
        
        GPSTracker gps = new GPSTracker(this, MainActivity.this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            if (gps.canGetLocation()) {
                double lattitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                RequestParams params = new RequestParams();
                params.put("lat", String.valueOf(lattitude));
                params.put("lng", String.valueOf(longitude));

                String url = "https://blood-help-india.herokuapp.com/nearby.php";
                String message = "Fetching data ...";

                httpRequest(getApplicationContext(), MainActivity.this, url, params, message);

            } else {
                gps.showSettingsAlert();
            }
        }

    }

    private void httpRequest(final Context mcontext, Activity activity, String url, RequestParams params, String message) {

        final ProgressDialog progressDialog = ProgressDialog.show(activity, null, message, false, false);

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                progressDialog.dismiss();

                try {
                    if (Objects.equals(response.getString("status"), "success")) {

                        Intent i = new Intent(MainActivity.this, MapsActivity.class);
                        i.putExtra("res", response.toString());
                        try {
                            i.putExtra("lat", response.getString("mylat"));
                            i.putExtra("lng", response.getString("mylng"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        change(i);

                    } else {
                        Toast.makeText(mcontext, response.getString("result"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {

                progressDialog.dismiss();

                Toast.makeText(mcontext, "network error", Toast.LENGTH_SHORT).show();
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