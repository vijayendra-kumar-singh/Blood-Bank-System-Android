package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

public class searchbanks extends AppCompatActivity implements AdapterView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    Snackbar snackbar;
    private String filter, filter_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchbanks);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        Intent p = getIntent();
        JSONObject o;

        try {
            o = new JSONObject(p.getStringExtra("data"));

            filter_value = o.getString("value");
            filter = o.getString("filter");

            TextView tx = (TextView) findViewById(R.id.mytext);
            tx.setText("Blood banks in " + filter_value);
            Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
            tx.setTypeface(custom_font);

            showData(o.getJSONArray("result"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void showData(JSONArray r) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try {

            for (int i = 0; i < r.length(); i++) {
                JSONObject jo = r.getJSONObject(i);
                String name = jo.getString("name");
                String contact = jo.getString("contact");
                String mobile = jo.getString("mobile");
                String email = jo.getString("email");
                String address = jo.getString("address");
                String city = jo.getString("city");
                String district = jo.getString("district");
                String pincode = jo.getString("pincode");
                String state = jo.getString("state");
                String website = jo.getString("website");
                String component = jo.getString("component");
                String apheresis = jo.getString("apheresis");
                String license = jo.getString("license");
                String lat = jo.getString("lat");
                String lng = jo.getString("lng");

                HashMap<String, String> banks = new HashMap<>();
                banks.put("name", name);
                banks.put("number", contact + ", " + mobile);
                banks.put("address", address);
                banks.put("website", website);
                banks.put("state", state);
                banks.put("city", city);
                banks.put("pincode", pincode);
                banks.put("email", email);
                banks.put("component", component);
                banks.put("apheresis", apheresis);
                banks.put("lat", lat);
                banks.put("lng", lng);
                banks.put("license", license);
                banks.put("district", district);

                list.add(banks);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListAdapter adapter = new SimpleAdapter(
                searchbanks.this, list, R.layout.banks,
                new String[]{"name"},
                new int[]{R.id.name});

        listView.setAdapter(adapter);
    }

    private void getData() {
        final ProgressDialog progressDialog = ProgressDialog.show(searchbanks.this, null, "Searching database ...", false, false);

        RequestParams params = new RequestParams();
        params.put("filter", filter);
        params.put("value", filter_value);

        String url = "https://blood-help-india.herokuapp.com/searchbloodbanks.php";

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
                        Toast.makeText(searchbanks.this, response.getString("result"), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(searchbanks.this, "network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        final HashMap map = (HashMap) adapterView.getItemAtPosition(i);

        Bundle b = new Bundle();
        b.putString("name", (String) map.get("name"));
        b.putString("number", (String) map.get("number"));
        b.putString("address", (String) map.get("address"));
        b.putString("email", (String) map.get("email"));
        b.putString("website", (String) map.get("website"));
        b.putString("state", (String) map.get("state"));
        b.putString("city", (String) map.get("city"));
        b.putString("district", (String) map.get("district"));
        b.putString("pincode", (String) map.get("pincode"));
        b.putString("lat", (String) map.get("lat"));
        b.putString("lng", (String) map.get("lng"));
        b.putString("apheresis", (String) map.get("apheresis"));
        b.putString("component", (String) map.get("component"));
        b.putString("license", (String) map.get("license"));

        Intent ii = new Intent(searchbanks.this, BankDetails.class);
        ii.putExtras(b);
        startActivity(ii);

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
