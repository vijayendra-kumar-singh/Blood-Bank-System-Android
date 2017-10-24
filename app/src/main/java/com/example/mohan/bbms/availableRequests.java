package com.example.mohan.bbms;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.navdrawer.SimpleSideDrawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

import static android.view.View.GONE;

public class availableRequests extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    SimpleSideDrawer slide_me;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Context context;
    GPSTracker gps;
    Snackbar snackbar;
    double lattitude, longitude;
    String year, month, date;

    EditText fp;
    Spinner fb;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_requests);

        if (ContextCompat.checkSelfPermission(availableRequests.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(availableRequests.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        context = this;

        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);

        final String utype = sharedPreferences.getString("utype", null);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        RelativeLayout nd = (RelativeLayout) findViewById(R.id.nd);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Objects.equals(utype, "Donor")) {
                    {
                        mSwipeRefreshLayout.setRefreshing(false);
                        checkConnection();
                    }
                } else {
                    recreate();
                }
            }
        });

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        //   expListView.setChildDivider(null);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                if (childPosition == 3) {
                    android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(availableRequests.this);
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

        slide_me = new SimpleSideDrawer(this);
        slide_me.setRightBehindContentView(R.layout.slide_menu);

        TextView profile = (TextView) findViewById(R.id.profile);
        TextView nearby = (TextView) findViewById(R.id.nearby);
        TextView search_user = (TextView) findViewById(R.id.searchdonor);
        TextView search_bank = (TextView) findViewById(R.id.search_bank);
        TextView post_request = (TextView) findViewById(R.id.postrequest);
        TextView my_requests = (TextView) findViewById(R.id.myrequest);
        TextView mypin = (TextView) findViewById(R.id.pin);
        TextView updateNumber = (TextView) findViewById(R.id.updateNumber);
        TextView updatePassword = (TextView) findViewById(R.id.updatepassword);
        TextView forgot = (TextView) findViewById(R.id.forgot);
        TextView logout = (TextView) findViewById(R.id.logout);

        ImageButton nearby_button = (ImageButton) findViewById(R.id.nearby_button);
        ImageButton search_donor_button = (ImageButton) findViewById(R.id.search_donor_button);
        ImageButton search_bank_button = (ImageButton) findViewById(R.id.search_bank_button);
        ImageButton post_button = (ImageButton) findViewById(R.id.post_button);
        TextView become_donor = (TextView) findViewById(R.id.become_donor);

        fb = (Spinner) findViewById(R.id.fb);
        fp = (EditText) findViewById(R.id.filterp);

        if (Objects.equals(utype, "Donor")) {
            nd.setVisibility(GONE);
            become_donor.setVisibility(GONE);
            tx.setText("Available Requests");
            checkConnection();
        } else if (Objects.equals(utype, "Non-Donor")) {
            tx.setText("Blood help");
            mSwipeRefreshLayout.setVisibility(GONE);
            nd.setVisibility(View.VISIBLE);
            become_donor.setVisibility(View.VISIBLE);
            nearby.setVisibility(GONE);
            search_user.setVisibility(GONE);
            fp.setVisibility(View.GONE);
            post_request.setVisibility(GONE);
            search_bank.setVisibility(GONE);
        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    profileClick();
                } else {
                    showSnack(false);
                }
            }
        });

        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    nearbyClick();
                } else {
                    showSnack(false);
                }
            }
        });

        search_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    searchUserClick();
                } else {
                    showSnack(false);
                }
            }
        });

        search_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    searchBankClick();
                } else {
                    showSnack(false);
                }
            }
        });

        post_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    postRequestClick();
                } else {
                    showSnack(false);
                }
            }
        });

        my_requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    myRequestsClick();
                } else {
                    showSnack(false);
                }
            }
        });

        mypin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    myPinClick();
                } else {
                    showSnack(false);
                }
            }
        });

        updateNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    updateNumberClick();
                } else {
                    showSnack(false);
                }
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    forgot();
                } else {
                    showSnack(false);
                }
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slide_me.toggleRightDrawer();
                if (ConnectivityReceiver.isConnected()) {
                    updatePasswordClick();
                } else {
                    showSnack(false);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        nearby_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                    nearbyClick();
                } else {
                    showSnack(false);
                }
            }
        });

        search_donor_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                    searchUserClick();
                } else {
                    showSnack(false);
                }
            }
        });

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                    postRequestClick();
                } else {
                    showSnack(false);
                }
            }
        });

        search_bank_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                    searchBankClick();
                } else {
                    showSnack(false);
                }
            }
        });

        become_donor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                    profileClick();
                } else {
                    showSnack(false);
                }
            }
        });

    }

    private boolean dateVerify(int year, int month, int date) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date today = calendar.getTime();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, date - 1);
        Date dateSpecified = calendar.getTime();
        return !dateSpecified.before(today);
    }

    private void searchBankClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.banksearch, null, true);

        final EditText filter = alertLayout.findViewById(R.id.filter);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        Spinner filter_type = alertLayout.findViewById(R.id.filtertype);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
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
                    filter.setText(null);
                    f[0] = "pincode";
                    filter.setMaxEms(6);
                    filter.setHint("Pincode");
                    filter.setInputType(InputType.TYPE_CLASS_NUMBER);
                    filter.setMaxLines(1);
                    setEditTextMaxLength(filter, 6);
                } else if (i == 2) {
                    f[0] = "city";
                    filter.setText(null);
                    filter.setMaxEms(25);
                    filter.setHint("City name");
                    filter.setInputType(InputType.TYPE_CLASS_TEXT);
                    filter.setMaxLines(1);
                    setEditTextMaxLength(filter, 50);
                } else if (i == 3) {
                    f[0] = "district";
                    filter.setMaxEms(25);
                    filter.setHint("District name");
                    filter.setText(null);
                    filter.setInputType(InputType.TYPE_CLASS_TEXT);
                    filter.setMaxLines(1);
                    setEditTextMaxLength(filter, 50);
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
                        Toast.makeText(context, "Invalid pincode", Toast.LENGTH_SHORT).show();
                    } else {
                        if (fil.length() < 3) {
                            Toast.makeText(context, "Invalid " + f[0] + " name", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            RequestParams params = new RequestParams();
                            params.put("filter", f[0]);
                            params.put("value", filter.getText().toString().trim().toLowerCase());

                            String url = "https://blood-help-india.herokuapp.com/searchbloodbanks.php";

                            String message = "Searching in database ...";

                            httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "searchBank");
                        }
                    }
                } else {
                    Toast.makeText(context, "Enter some data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void profileClick() {
        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        String user_number = sharedPreferences.getString("number", null);

        RequestParams params = new RequestParams();
        params.put("number", user_number);

        String message = "Fetching your details ...";

        String url = "https://blood-help-india.herokuapp.com/getprofile.php";

        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "getProfile");
    }

    private void updatePasswordClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.updatepassword, null, true);
        final EditText oldpass = alertLayout.findViewById(R.id.oldpass);
        final EditText newpass = alertLayout.findViewById(R.id.newpass);

        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);

        final CheckBox show = alertLayout.findViewById(R.id.show);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    oldpass.setTransformationMethod(null);
                    newpass.setTransformationMethod(null);
                } else {
                    oldpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((oldpass.getText().toString().trim()).length() < 6) {
                    Toast.makeText(availableRequests.this, "Invalid current password", Toast.LENGTH_SHORT).show();
                } else {
                    if ((oldpass.getText().toString().trim()).length() < 6) {
                        Toast.makeText(availableRequests.this, "Password must have at least 6 digits", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                        String user_number = sharedPreferences.getString("number", null);

                        String cp = oldpass.getText().toString().trim();
                        String np = newpass.getText().toString().trim();

                        RequestParams params = new RequestParams();
                        params.put("old", cp);
                        params.put("number", user_number);
                        params.put("new", np);

                        String url = "https://blood-help-india.herokuapp.com/updatepassword.php";

                        String message = "Processing";

                        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "updatePassword");
                    }
                }
            }
        });
    }

    private void updateNumberClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.number, null, true);
        final EditText number = alertLayout.findViewById(R.id.num);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
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
                    Toast.makeText(availableRequests.this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();

                    RequestParams params = new RequestParams();
                    params.put("number", num);
                    String url = "https://blood-help-india.herokuapp.com/otp.php";
                    String message = "Checking ...";

                    httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "checkNumber");
                }
            }
        });
    }

    private void myPinClick() {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(availableRequests.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps = new GPSTracker(context, availableRequests.this);
            if (gps.canGetLocation()) {
                lattitude = gps.getLatitude();
                longitude = gps.getLongitude();

                RequestParams params = new RequestParams();
                params.put("lat", String.valueOf(lattitude));
                params.put("lng", String.valueOf(longitude));

                String url = "https://blood-help-india.herokuapp.com/getpincode.php";
                String message = "Locating you ...";

                httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "myPin");

            } else {
                gps.showSettingsAlert();
            }
        }
    }

    private void myRequestsClick() {

        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        String user_number = sharedPreferences.getString("number", null);

        RequestParams params = new RequestParams();
        params.put("number", user_number);

        String url = "https://blood-help-india.herokuapp.com/myrequests.php";

        String message = "Fetching your requests ...";

        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "myRequests");
    }

    private void postRequestClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.request, null, true);
        final EditText nname = alertLayout.findViewById(R.id.name);
        final EditText nnumber = alertLayout.findViewById(R.id.num);
        final EditText ppincode = alertLayout.findViewById(R.id.pincode);
        final TextView ddd = alertLayout.findViewById(R.id.date);
        final EditText uunit = alertLayout.findViewById(R.id.unit);

        final Spinner btype = alertLayout.findViewById(R.id.btype);

        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button post = alertLayout.findViewById(R.id.postrequest);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final String[] bt = new String[1];

        List<String> groups = new ArrayList<>();
        groups.add("select");
        groups.add("A+");
        groups.add("A-");
        groups.add("B+");
        groups.add("B-");
        groups.add("AB+");
        groups.add("AB-");
        groups.add("O+");
        groups.add("O-");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        btype.setAdapter(dataAdapter);

        btype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String g = adapterView.getItemAtPosition(i).toString();
                bt[0] = (g.replace("+", "1").replace("-", "0"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                bt[0] = "None";
            }
        });


        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                year = String.valueOf(i);
                month = String.valueOf(i1 + 1);
                date = String.valueOf(i2);
                boolean y = dateVerify(i, i1 + 1, i2);
                if (y) {
                    ddd.setText(date + "-" + month + "-" + year);
                } else {
                    Toast.makeText(context, "Invalid date", Toast.LENGTH_SHORT).show();
                }
            }
        };


        ddd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(availableRequests.this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = nname.getText().toString().trim();
                String u = uunit.getText().toString().trim();
                String num = nnumber.getText().toString().trim();
                String p = ppincode.getText().toString().trim();
                String d = ddd.getText().toString().trim();

                if (n.length() < 3) {
                    Toast.makeText(availableRequests.this, "Name must be of at least 3 characters", Toast.LENGTH_SHORT).show();
                } else {
                    if (bt[0].length() > 3) {
                        Toast.makeText(availableRequests.this, "Invalid Blood group", Toast.LENGTH_SHORT).show();
                    } else {
                        if (u.length() < 1 || u.length() > 2) {
                            Toast.makeText(availableRequests.this, "Invalid number of units", Toast.LENGTH_SHORT).show();
                        } else {
                            if (num.length() != 10) {
                                Toast.makeText(availableRequests.this, "Invalid contact number", Toast.LENGTH_SHORT).show();
                            } else {
                                if (p.length() != 6) {
                                    Toast.makeText(availableRequests.this, "Invalid pincode", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (d.length() == 4) {
                                        Toast.makeText(availableRequests.this, "Select date", Toast.LENGTH_SHORT).show();
                                    } else {
                                        dialog.dismiss();
                                        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                                        String user_number = sharedPreferences.getString("number", null);

                                        RequestParams params = new RequestParams();
                                        params.put("name", n);
                                        params.put("number", num);
                                        params.put("btype", bt[0]);
                                        params.put("pincode", p);
                                        params.put("units", u);
                                        params.put("r_number", user_number);
                                        params.put("date", d);

                                        String url = "https://blood-help-india.herokuapp.com/request.php";

                                        String message = "Posting request ...";

                                        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "postRequest");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void searchUserClick() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.usersearch, null, true);
        final EditText pincode = alertLayout.findViewById(R.id.pincode);

        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        final Spinner btype = alertLayout.findViewById(R.id.btype);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final String[] pin = new String[1];
        final String[] bt = new String[1];

        List<String> groups = new ArrayList<>();
        groups.add("select");
        groups.add("A+");
        groups.add("A-");
        groups.add("B+");
        groups.add("B-");
        groups.add("AB+");
        groups.add("AB-");
        groups.add("O+");
        groups.add("O-");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        btype.setAdapter(dataAdapter);

        btype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String g = adapterView.getItemAtPosition(i).toString();
                bt[0] = (g.replace("+", "1").replace("-", "0"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                bt[0] = "None";
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
                pin[0] = pincode.getText().toString().trim();

                if (pin[0].length() != 6) {
                    Toast.makeText(availableRequests.this, "Invalid pincode", Toast.LENGTH_SHORT).show();
                } else {
                    if (bt[0].length() > 3) {
                        Toast.makeText(availableRequests.this, "Invalid Blood group", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();

                        SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                        String user_number = sharedPreferences.getString("number", null);

                        RequestParams params = new RequestParams();
                        params.put("number", user_number);
                        params.put("pincode", pin[0]);
                        params.put("btype", bt[0]);

                        String url = "https://blood-help-india.herokuapp.com/searchusers.php";

                        String message = "Searching donors ...";

                        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "donorSearch");
                    }
                }
            }
        });
    }

    private void nearbyClick() {

        gps = new GPSTracker(context, availableRequests.this);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(availableRequests.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            if (gps.canGetLocation()) {
                lattitude = gps.getLatitude();
                longitude = gps.getLongitude();
                RequestParams params = new RequestParams();
                params.put("lat", String.valueOf(lattitude));
                params.put("lng", String.valueOf(longitude));

                String url = "https://blood-help-india.herokuapp.com/nearby.php";
                String message = "Fetching data ...";

                httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "nearby");

            } else {
                gps.showSettingsAlert();
            }
        }

    }

    private void showProfile(JSONObject response) throws JSONException {

        JSONArray arr = response.getJSONArray("result");
        final JSONObject data = arr.getJSONObject(0);

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.profile, null, true);
        final TextView name = alertLayout.findViewById(R.id.name);
        final TextView number = alertLayout.findViewById(R.id.num);
        final TextView pincode = alertLayout.findViewById(R.id.pincode);
        final TextView age = alertLayout.findViewById(R.id.age);
        final TextView btype = alertLayout.findViewById(R.id.btype);
        final Button edit = alertLayout.findViewById(R.id.edit);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        name.setText(data.getString("name"));
        number.setText(data.getString("number"));
        age.setText(data.getString("age") + " years");
        pincode.setText(data.getString("pincode"));
        btype.setText(((data.getString("btype")).replace("0", " -")).replace("1", " +"));

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.edit_profile, null, true);
                final EditText name = alertLayout.findViewById(R.id.name);
                final EditText pincode = alertLayout.findViewById(R.id.pincode);
                final EditText age = alertLayout.findViewById(R.id.age);
                final EditText password = alertLayout.findViewById(R.id.password);
                final Spinner btype = alertLayout.findViewById(R.id.btype);

                final Button cancel = alertLayout.findViewById(R.id.cancel);
                final Button save = alertLayout.findViewById(R.id.save);

                final CheckBox check = alertLayout.findViewById(R.id.donor);

                AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
                alert.setView(alertLayout);
                alert.setCancelable(false);
                final AlertDialog dialog = alert.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                final String bt[] = new String[1];
                final String[] uty = new String[1];
                final String id[] = new String[1];

                List<String> groups = new ArrayList<>();
                groups.add("select");
                groups.add("A+");
                groups.add("A-");
                groups.add("B+");
                groups.add("B-");
                groups.add("AB+");
                groups.add("AB-");
                groups.add("O+");
                groups.add("O-");

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, groups);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                btype.setAdapter(dataAdapter);

                btype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String b = adapterView.getItemAtPosition(i).toString();
                        bt[0] = (b.replace("+", "1")).replace("-", "0");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        bt[0] = "None";
                    }
                });

                try {
                    if (Objects.equals(data.getString("utype"), "Donor")) {
                        check.setVisibility(GONE);
                        uty[0] = "Donor";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String string = null;
                try {
                    string = data.getString("btype");
                    id[0] = data.getString("id");
                    name.setText(data.getString("name"));
                    age.setText(data.getString("age"));
                    pincode.setText(data.getString("pincode"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (Objects.equals(string, "A+")) {
                    btype.setSelection(1);
                } else if (Objects.equals(string, "A-")) {
                    btype.setSelection(2);
                } else if (Objects.equals(string, "B+")) {
                    btype.setSelection(3);
                } else if (Objects.equals(string, "B-")) {
                    btype.setSelection(4);
                } else if (Objects.equals(string, "AB+")) {
                    btype.setSelection(5);
                } else if (Objects.equals(string, "AB-")) {
                    btype.setSelection(6);
                } else if (Objects.equals(string, "O+")) {
                    btype.setSelection(7);
                } else if (Objects.equals(string, "O-")) {
                    btype.setSelection(8);
                }

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            uty[0] = "Donor";
                        } else {
                            uty[0] = "Non-Donor";
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if ((name.getText().toString().trim()).length() < 3) {
                            Toast.makeText(availableRequests.this, "Name must be of at least 3 letters", Toast.LENGTH_SHORT).show();
                        } else {
                            if (Integer.parseInt(age.getText().toString().trim()) > 65 || Integer.parseInt(age.getText().toString().trim()) < 18) {
                                Toast.makeText(availableRequests.this, "Age must be between 18 - 25", Toast.LENGTH_SHORT).show();
                            } else {
                                if ((pincode.getText().toString().trim()).length() != 6) {
                                    Toast.makeText(availableRequests.this, "Invalid pincode", Toast.LENGTH_SHORT).show();
                                } else {
                                    if ((password.getText().toString().trim()).length() < 6) {
                                        Toast.makeText(availableRequests.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (bt[0].length() > 3) {
                                            Toast.makeText(availableRequests.this, "Select your blood group", Toast.LENGTH_SHORT).show();
                                        } else {
                                            dialog.dismiss();

                                            RequestParams params = new RequestParams();
                                            params.put("id", id[0]);
                                            params.put("name", name.getText().toString().trim());
                                            params.put("age", age.getText().toString().trim());
                                            params.put("pincode", pincode.getText().toString().trim());
                                            params.put("btype", bt[0]);
                                            params.put("utype", uty[0]);
                                            params.put("password", password.getText().toString().trim());

                                            String url = "https://blood-help-india.herokuapp.com/updateprofile.php";

                                            String message = "Updating details ...";

                                            httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "updateProfile");
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void updateNumber(final String num, final String base, final String otp) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.otp, null, true);
        final EditText OTP = alertLayout.findViewById(R.id.num);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        OTP.setText(otp);

        OTP.setMaxEms(6);

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();

                RequestParams params = new RequestParams();
                params.put("number", num);

                String url = "https://blood-help-india.herokuapp.com/abort.php";
                String message = "Wait ...";

                httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "abort");
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = OTP.getText().toString();
                if (otp.length() != 6) {
                    Toast.makeText(availableRequests.this, "Enter correct OTP", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();

                    RequestParams params = new RequestParams();
                    params.put("otp", otp);
                    params.put("number", num);
                    params.put("base", base);

                    String url = "https://blood-help-india.herokuapp.com/updatenumber.php";
                    String message = "Verifying ...";

                    httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "updateNumber");
                }
            }
        });
    }

    private void forgot() {
        SharedPreferences sharedPreference = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        String num = sharedPreference.getString("number", null);
        RequestParams params = new RequestParams();
        params.put("number", num);
        String url = "https://blood-help-india.herokuapp.com/forgototp.php";
        String message = "Sending OTP ...";

        httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "forgot");

    }

    private void logout() {

        slide_me.toggleRightDrawer();
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.logout, null, true);
        final Button yes = alertLayout.findViewById(R.id.yes);
        final Button no = alertLayout.findViewById(R.id.no);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.putString("number", null);
                editor.apply();
                Intent intent = new Intent(availableRequests.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                change(intent);
                finish();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void availablereq(String user_number) {

        RequestParams params = new RequestParams();
        params.put("number", user_number);

        String url = "https://blood-help-india.herokuapp.com/availablerequests.php";
        String message = "Fetching available requests...";

        httpRequest(getApplication(), availableRequests.this, url, params, message, "availableRequests");
    }

    private void showreq(JSONArray r) {

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        int count = 0;

        try {

            for (int i = 0; i < r.length(); i++) {
                JSONObject jo = r.getJSONObject(i);
                String name = jo.getString("name");
                String number = jo.getString("number");
                String btype = (((jo.getString("btype")).replace("0", " -")).replace("1", " +"));
                String unit = jo.getString("units") + " units";
                String pincode = jo.getString("pincode");
                String date = jo.getString("date");

                if (fp.getText().toString().trim().length() == 6) {
                    if (Objects.equals(fb.getSelectedItem().toString(), "select")) {
                        if (Objects.equals(fp.getText().toString().trim(), pincode)) {
                            listDataHeader.add(name);

                            List<String> top250 = new ArrayList<>();
                            top250.add("Requirement : " + unit + " of " + btype + " type.");
                            top250.add("Date : " + date);
                            top250.add("Pincode : " + pincode);
                            top250.add("Number : " + number);

                            listDataChild.put(listDataHeader.get(count), top250);
                            count++;
                        }
                    } else if (!Objects.equals(fb.getSelectedItem().toString(), "select")) {
                        if (Objects.equals(fp.getText().toString().trim(), pincode) && Objects.equals(fb.getSelectedItem().toString(), btype)) {
                            listDataHeader.add(name);

                            List<String> top250 = new ArrayList<>();
                            top250.add("Requirement : " + unit + " of " + btype + " type.");
                            top250.add("Date : " + date);
                            top250.add("Pincode : " + pincode);
                            top250.add("Number : " + number);

                            listDataChild.put(listDataHeader.get(count), top250);
                            count++;
                        }
                    }
                } else if (fp.getText().toString().trim().length() != 6) {
                    if (!Objects.equals(fb.getSelectedItem().toString(), "select")) {
                        if (Objects.equals(fb.getSelectedItem().toString(), btype)) {
                            listDataHeader.add(name);

                            List<String> top250 = new ArrayList<>();
                            top250.add("Requirement : " + unit + " of " + btype + " type.");
                            top250.add("Date : " + date);
                            top250.add("Pincode : " + pincode);
                            top250.add("Number : " + number);

                            listDataChild.put(listDataHeader.get(count), top250);
                            count++;
                        }
                    } else if (Objects.equals(fb.getSelectedItem().toString(), "select")) {
                        listDataHeader.add(name);

                        List<String> top250 = new ArrayList<>();
                        top250.add("Requirement : " + unit + " of " + btype + " type.");
                        top250.add("Date : " + date);
                        top250.add("Pincode : " + pincode);
                        top250.add("Number : " + number);

                        listDataChild.put(listDataHeader.get(count), top250);
                        count++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);
    }

    private void httpRequest(final Context mcontext, Activity activity, String url, RequestParams params, String message, final String functionName) {

        final ProgressDialog progressDialog = ProgressDialog.show(activity, null, message, false, false);

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                progressDialog.dismiss();

                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        selector(response, functionName);
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

    private void selector(JSONObject response, String functionName) {

        if (Objects.equals(functionName, "getProfile")) {
            try {
                showProfile(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(functionName, "updateProfile")) {
            SharedPreferences sharedPreference = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreference.edit();
            try {
                editor.putString("utype", (response.getString("utype")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.apply();
            Toast.makeText(this, "Details updated", Toast.LENGTH_SHORT).show();
            recreate();
        } else if (Objects.equals(functionName, "donorSearch")) {
            Intent o = new Intent(availableRequests.this, donorSearch.class);
            o.putExtra("data", response.toString());
            change(o);
        } else if (Objects.equals(functionName, "postRequest")) {
            Toast.makeText(this, "Request posted successfully", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(functionName, "myRequests")) {
            Intent o = new Intent(availableRequests.this, myrequests.class);
            o.putExtra("data", response.toString());
            change(o);
        } else if (Objects.equals(functionName, "myPin")) {
            String mypin = null;
            try {
                mypin = response.getString("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.pin, null, true);
            final TextView myp = alertLayout.findViewById(R.id.showpin);

            final Button ok = alertLayout.findViewById(R.id.ok);

            AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
            alert.setView(alertLayout);
            alert.setCancelable(false);
            final AlertDialog dialog = alert.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            myp.setText("Your pincode is " + mypin);

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        } else if (Objects.equals(functionName, "updatePassword")) {
            Toast.makeText(availableRequests.this, "Password updated", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(functionName, "checkNumber")) {
            SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
            String base = sharedPreferences.getString("number", null);
            try {
                updateNumber(response.getString("number"), base, response.getString("otp"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(functionName, "updateNumber")) {
            SharedPreferences sharedPreference = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreference.edit();
            try {
                editor.putString("number", (response.getString("number")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.apply();
            Toast.makeText(this, "Number updated", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(functionName, "abort")) {
            Toast.makeText(availableRequests.this, "Process canceled", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(functionName, "nearby")) {
            Intent i = new Intent(availableRequests.this, MapsActivity.class);
            i.putExtra("res", response.toString());
            try {
                i.putExtra("lat", response.getString("mylat"));
                i.putExtra("lng", response.getString("mylng"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            change(i);
        } else if (Objects.equals(functionName, "availableRequests")) {
            try {
                showreq(response.getJSONArray("result"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(functionName, "searchBank")) {
            Intent i = new Intent(availableRequests.this, searchbanks.class);
            i.putExtra("data", response.toString());
            change(i);
        } else if (Objects.equals(functionName, "forgot")) {
            SharedPreferences sharedPreference = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
            String num = sharedPreference.getString("number", null);
            try {
                reset(num, response.getString("otp"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (Objects.equals(functionName, "reset")) {
            String mypin = null;
            try {
                mypin = response.getString("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.pin, null, true);
            final TextView myp = alertLayout.findViewById(R.id.showpin);

            final Button ok = alertLayout.findViewById(R.id.ok);

            AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
            alert.setView(alertLayout);
            alert.setCancelable(false);
            final AlertDialog dialog = alert.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            myp.setText("Your new Password is " + mypin);

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void reset(final String num, String Otp) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.otp, null, true);
        final EditText number = alertLayout.findViewById(R.id.num);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button ok = alertLayout.findViewById(R.id.ok);

        AlertDialog.Builder alert = new AlertDialog.Builder(availableRequests.this);
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

                RequestParams params = new RequestParams();
                params.put("number", num);

                String url = "https://blood-help-india.herokuapp.com/abort.php";
                String message = "Wait ...";

                httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "abort");
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String otp = number.getText().toString();
                if (otp.length() != 6) {
                    Toast.makeText(availableRequests.this, "Enter correct OTP", Toast.LENGTH_SHORT).show();
                } else {
                    RequestParams params = new RequestParams();
                    params.put("otp", otp);
                    params.put("number", num);
                    String url = "https://blood-help-india.herokuapp.com/forgot.php";
                    String message = "Verifying ...";

                    httpRequest(getApplicationContext(), availableRequests.this, url, params, message, "reset");
                }
            }
        });
    }

    private void change(final Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, 1);
    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(availableRequests.this);

        alertDialog.setMessage("Calling permission needed for this service.");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                change(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(availableRequests.this);
        alertDialog.setMessage("Are you sure you want to leave?");
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        change(intent);
                        finish();
                    }
                });
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
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
            SharedPreferences sharedPreferences = availableRequests.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
            String user_number = sharedPreferences.getString("number", null);
            availablereq(user_number);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        //Adding our menu to toolbar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu) {
            slide_me.toggleRightDrawer();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        backButtonHandler();
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