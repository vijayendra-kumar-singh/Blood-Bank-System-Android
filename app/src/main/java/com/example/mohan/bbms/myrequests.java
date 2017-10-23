package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

public class myrequests extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private String number;
    Snackbar snackbar;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myrequests);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        tx.setText("My requests");
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        SharedPreferences sharedPreferences = myrequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        number = sharedPreferences.getString("number", null);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                checkConnection();
            }
        });

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
//        expListView.setChildDivider(null);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(myrequests.this);

                alertDialog.setMessage("Do you want to delete this request?");

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String h = listDataChild.get(listDataHeader.get(groupPosition)).get(0);
                        deleteRequest(h.split(":")[1].trim());
                    }
                });

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();

                return false;
            }
        });

        Intent I = getIntent();

        try {
            JSONObject js = new JSONObject(I.getStringExtra("data"));
            showreq(js.getJSONArray("result"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getData() {

        RequestParams params = new RequestParams();

        params.put("number", number);

        final ProgressDialog progressDialog = ProgressDialog.show(myrequests.this, null, "Fetching Your requests...", false, false);

        String url = "https://blood-help-india.herokuapp.com/myrequests.php";

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        showreq(response.getJSONArray("result"));
                    } else {
                        Toast.makeText(myrequests.this, response.getString("result"), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Toast.makeText(myrequests.this, "network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showreq(JSONArray r) {

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        try {

            for (int i = 0; i < r.length(); i++) {
                JSONObject jo = r.getJSONObject(i);
                String name = jo.getString("name");
                String number = jo.getString("number");
                String btype = (((jo.getString("btype")).replace("0", " -")).replace("1", " +"));
                String unit = jo.getString("units") + " units";
                String pincode = jo.getString("pincode");
                String date = jo.getString("date");
                String id = jo.getString("id");

                listDataHeader.add(name);

                List<String> top250 = new ArrayList<>();
                top250.add("Request ID : " + id);
                top250.add("Requirement : " + unit + " " + btype + " type.");
                top250.add("Date : " + date);
                top250.add("Pincode : " + pincode);
                top250.add("Number : " + number);

                listDataChild.put(listDataHeader.get(i), top250);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);
    }

    private void deleteRequest(String id) {

        RequestParams params = new RequestParams();
        params.put("id", id);

        final ProgressDialog progressDialog = ProgressDialog.show(myrequests.this, null, "Processing ...", false, false);

        String url = "https://blood-help-india.herokuapp.com/deleterequest.php";

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        Toast.makeText(myrequests.this, "Request deleted", Toast.LENGTH_SHORT).show();
                        getData();
                    } else {
                        Toast.makeText(myrequests.this, "server error", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Toast.makeText(myrequests.this, "network error", Toast.LENGTH_SHORT).show();
            }
        });
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
