package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class myrequests extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    private String number;
    Snackbar snackbar;

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

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        listView.setDivider(null);
        listView.setDividerHeight(0);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                checkConnection();
            }
        });

        Intent I = getIntent();

        try {
            JSONObject js = new JSONObject(I.getStringExtra("data"));
            show(js.getJSONArray("result"));
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
                        show(response.getJSONArray("result"));
                    } else {
                        Toast.makeText(myrequests.this, response.getString("result"), Toast.LENGTH_SHORT).show();
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

    private void show(JSONArray r) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();
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

                HashMap<String, String> requests = new HashMap<>();
                requests.put("name", name);
                requests.put("number", number);
                requests.put("units", unit);
                requests.put("btype", btype);
                requests.put("pincode", pincode);
                requests.put("date", date);
                requests.put("id", id);

                list.add(requests);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListAdapter adapter = new SimpleAdapter(
                myrequests.this, list, R.layout.myrequests,
                new String[]{"number", "name", "units", "pincode", "btype","date"},
                new int[]{R.id.num, R.id.name, R.id.unit, R.id.pincode, R.id.btype, R.id.date});

        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final HashMap map = (HashMap) adapterView.getItemAtPosition(i);

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(myrequests.this);

        alertDialog.setMessage("Do you want to delete this request?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteRequest((String) map.get("id"));
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void deleteRequest(String id){

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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final HashMap map = (HashMap) parent.getItemAtPosition(position);

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(myrequests.this);

        alertDialog.setMessage("Do you want to delete this request?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteRequest((String) map.get("id"));
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();

        return true;
    }
}
