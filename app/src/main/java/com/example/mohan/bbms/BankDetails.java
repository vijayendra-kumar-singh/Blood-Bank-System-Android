package com.example.mohan.bbms;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class BankDetails extends AppCompatActivity {

    TextView name, contact, email, web, pin, city, state, district, apheresis, component, license, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_details);

        Intent o = getIntent();
        Bundle g = o.getExtras();

        name = (TextView) findViewById(R.id.name);
        contact = (TextView) findViewById(R.id.contact);
        email = (TextView) findViewById(R.id.email);
        web = (TextView) findViewById(R.id.website);
        pin = (TextView) findViewById(R.id.pincode);
        city = (TextView) findViewById(R.id.city);
        district = (TextView) findViewById(R.id.district);
        state = (TextView) findViewById(R.id.state);
        component = (TextView) findViewById(R.id.component);
        apheresis = (TextView) findViewById(R.id.apheresis);
        license = (TextView) findViewById(R.id.license);
        address = (TextView) findViewById(R.id.address);


        name.setText(g.getString("name"));
        contact.setText(g.getString("number"));
        email.setText(g.getString("email"));
        web.setText(g.getString("website"));
        pin.setText(g.getString("pincode"));
        city.setText(g.getString("city"));
        district.setText(g.getString("district"));
        state.setText(g.getString("state"));
        component.setText(g.getString("component"));
        apheresis.setText(g.getString("apheresis"));
        license.setText(g.getString("license"));
        address.setText(g.getString("address"));

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        TextView tx = (TextView) findViewById(R.id.mytext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
        tx.setTypeface(custom_font);

        tx.setText(g.getString("name"));

    }
}
