package com.example.mohan.bbms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        tx.setText("Register");
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        Intent o = getIntent();
        final String num = o.getStringExtra("number");

        final EditText name = (EditText) findViewById(R.id.name);
        final EditText age = (EditText) findViewById(R.id.age);
        final EditText pincode = (EditText) findViewById(R.id.pincode);
        final EditText password = (EditText) findViewById(R.id.password);

        TextView number = (TextView) findViewById(R.id.num);
        TextView signUpLink = (TextView) findViewById(R.id.sign_in_link);
        final Spinner btype = (Spinner) findViewById(R.id.btype);

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

        Button register = (Button) findViewById(R.id.buttonRegister);

        number.setText(num);

        CheckBox donor = (CheckBox) findViewById(R.id.donor);

        final String[] utype = new String[1];

        donor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    utype[0] = "Donor";
                } else {
                    utype[0] = "Non-Donor";
                }
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change(new Intent(register.this, login.class));
                finish();
            }
        });

        final String[] sexx = {null};
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiosex);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
                sexx[0] = checkedRadioButton.getText().toString();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString().trim();
                String a = age.getText().toString().trim();
                String pin = pincode.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String sex = null;

                if (Objects.equals(sexx[0], "M")) {
                    sex = "Male";
                } else if (Objects.equals(sexx[0], "F")) {
                    sex = "Female";
                }

                if (utype[0] == null) {
                    verify(n, a, pin, pass, num, bt[0], "Non-Donor", sex);
                } else {
                    verify(n, a, pin, pass, num, bt[0], utype[0], sex);
                }
            }
        });

    }

    private void verify(String n, String a, String pin, String pass, String num, String bt, String s, String sex) {
        if (Objects.equals(n, "") || n.length() < 3) {
            Toast.makeText(this, "Name must be of at least 3 letters", Toast.LENGTH_SHORT).show();
        } else {
            if (Objects.equals(a, "") || Integer.parseInt(a) > 65 || Integer.parseInt(a) < 18) {
                Toast.makeText(this, "Age must be between 18 ~ 65", Toast.LENGTH_SHORT).show();
            } else {
                if (pin.length() != 6) {
                    Toast.makeText(this, "Invalid pincode", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.length() < 6) {
                        Toast.makeText(this, "Password must be of at least 6 digits", Toast.LENGTH_SHORT).show();
                    } else {
                        if (bt.length() > 3) {
                            Toast.makeText(this, "Choose your blood group", Toast.LENGTH_SHORT).show();
                        } else {
                            if (Objects.equals(sex, "Male") || Objects.equals(sex, "Female")) {
                                reg(n, a, pin, pass, num, bt, s, sex);
                            } else {
                                Toast.makeText(this, "Select your gender!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }
    }

    private void reg(String n, String a, String pin, String pass, final String num, String bt, String s, String sex) {

        FirebaseMessaging.getInstance().subscribeToTopic("requests");
        String token = new tokenGenerator().getToken();

        RequestParams params = new RequestParams();
        params.put("name", n);
        params.put("number", num);
        params.put("pincode", pin);
        params.put("utype", s);
        params.put("btype", bt);
        params.put("password", pass);
        params.put("age", a);
        params.put("sex", sex);
        params.put("token", token);

        String url = "https://blood-help-india.herokuapp.com/register.php";

        final ProgressDialog progressDialog = ProgressDialog.show(register.this, null, "Registering ...", false, false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------success------", "---------success------" + String.valueOf(response));
                try {
                    if (Objects.equals(response.getString("status"), "success")) {
                        SharedPreferences sharedPreferences = register.this.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("number", num);
                        editor.putString("utype", response.getString("result"));
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(), availableRequests.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        change(intent);
                    } else {
                        Toast.makeText(register.this, "server error", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                progressDialog.dismiss();
                Log.i("---------fail----------", "---------fail------" + response);
                Toast.makeText(register.this, "network error", Toast.LENGTH_SHORT).show();
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
