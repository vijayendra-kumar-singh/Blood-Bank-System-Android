package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
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
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class donorSearch extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private String btype, pincode, number;
    Snackbar snackbar;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;


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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        Intent I = getIntent();

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
       // expListView.setChildDivider(null);

        try {
            JSONObject js = new JSONObject(I.getStringExtra("data"));
            showData(js.getJSONArray("result"));
            JSONObject jo = (js.getJSONArray("result")).getJSONObject(0);
            btype = jo.getString("btype");
            pincode = jo.getString("pincode");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                if (childPosition == 1) {
                    android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(donorSearch.this);
                    alertDialog.setMessage("Do you want to call " + listDataHeader.get(groupPosition) + "?");

                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_CALL);

                            String nn = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                            intent.setData(Uri.parse("tel:" + "+91" + nn.split(":")[1].trim()));
                            if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                return false;
            }
        });

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
                        showData(response.getJSONArray("result"));
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

    private void showData(JSONArray r) {

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        try {

            for (int i = 0; i < r.length(); i++) {
                JSONObject jo = r.getJSONObject(i);
                String name = jo.getString("name");
                String number = jo.getString("number");
                String age = jo.getString("age");
                String sex = jo.getString("sex");

                listDataHeader.add(name);

                List<String> top250 = new ArrayList<>();
                top250.add("Age/Sex : " + age + "years (" + sex + ")");
                top250.add("Number  : " + number);

                listDataChild.put(listDataHeader.get(i), top250);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
