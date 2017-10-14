package com.example.mohan.bbms;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class donorSearch extends AppCompatActivity implements AdapterView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    private String btype, pincode, number;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresults);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        tx.setText("Donors list");
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        SharedPreferences sharedPreferences = donorSearch.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        number = sharedPreferences.getString("number", null);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        listView.setDivider(null);
        listView.setDividerHeight(0);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        Intent I = getIntent();

        try {
            JSONObject js = new JSONObject(I.getStringExtra("data"));
            show(js.getJSONArray("result"));
            JSONObject jo = (js.getJSONArray("result")).getJSONObject(0);
            btype = jo.getString("btype");
            pincode = jo.getString("pincode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getData() {

        final ProgressDialog progressDialog = ProgressDialog.show(donorSearch.this, null, "Searching donors ...", false, false);

        RequestParams params = new RequestParams();
        params.put("number", number);
        params.put("pincode", pincode);
        params.put("btype", btype);

        String url = "https://blood-help-india.herokuapp.com/searchusers.php";

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------success------", "---------success------" + String.valueOf(response));
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        show(response.getJSONArray("result"));
                    } else {
                        Toast.makeText(donorSearch.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(donorSearch.this, "network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void show(JSONArray r) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try {

            for (int i = 0; i < r.length(); i++) {
                JSONObject jo = r.getJSONObject(i);
                String name = jo.getString("name");
                String number = jo.getString("number");
                String blood = (((jo.getString("btype")).replace("0", " -")).replace("1", " +"));
                String age = jo.getString("age");
                String sex = jo.getString("sex");
                String pincode = jo.getString("pincode");


                HashMap<String, String> donors = new HashMap<>();
                donors.put("name", name);
                donors.put("number", number);
                donors.put("age", age + " years");
                donors.put("blood", blood);
                donors.put("sex", sex);
                donors.put("pincode", pincode);

                list.add(donors);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListAdapter adapter = new SimpleAdapter(
                donorSearch.this, list, R.layout.donor,
                new String[]{"name"},
                new int[]{R.id.name});

        listView.setAdapter(adapter);
    }

    private void showData(JSONArray r){

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final HashMap map = (HashMap) adapterView.getItemAtPosition(i);

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.donors, null, true);

        final TextView name = alertLayout.findViewById(R.id.name);
        final TextView age = alertLayout.findViewById(R.id.age);
        final TextView gender = alertLayout.findViewById(R.id.gender);
        final TextView number = alertLayout.findViewById(R.id.number);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        name.setText((String) map.get("name"));
        age.setText((String) map.get("age"));
        gender.setText((String) map.get("sex"));
        number.setText((String) map.get("number"));

        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(donorSearch.this);
                alertDialog.setMessage("Do you want to call " + map.get("name") + "?");

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + "+91" + map.get("number")));
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            showSettingsAlert();
                            return;
                        }
                        startActivity(intent);

                    }
                });

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });
    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(donorSearch.this);

        alertDialog.setMessage("Calling permission needed for this service.");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            getData();
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.WHITE;
            snackbar = Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
