package com.example.mohan.bbms;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private static final int MAP_ZOOM_LEVEL = 11;
    private GoogleMap mMap;
    private JSONArray data;
    private LatLng myLatlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.actionbar);
//
//        TextView tx = (TextView) findViewById(R.id.mytext);
//        tx.setText("Blood banks near you");
//        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Billabong.ttf");
//        tx.setTypeface(custom_font);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        try {
            JSONObject res = new JSONObject(i.getStringExtra("res"));
            data = res.getJSONArray("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        myLatlng = new LatLng(Double.parseDouble(i.getStringExtra("lat")), Double.parseDouble(i.getStringExtra("lng")));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            return;
//        }

        //mMap.setMyLocationEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                myLatlng,
                MAP_ZOOM_LEVEL));
        mMap.addMarker(new MarkerOptions()
                .position(myLatlng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Your Location"));

        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject explrObject = data.getJSONObject(i);
                Log.d("-=-=-=", String.valueOf(explrObject));
                LatLng latLng = new LatLng(Double.valueOf(explrObject.getString("lat")), Double.valueOf(explrObject.getString("lng")));
                MarkerOptions mMarkerOption = new MarkerOptions()
                        .position(latLng)
                        .title(explrObject.getString("name"));
                mMap.addMarker(mMarkerOption);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        Log.d("----title", title);

        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject explrObject = data.getJSONObject(i);
                Log.d("----name", explrObject.getString("name"));
                if (explrObject.getString("name").equals(title)) {
                    showdetails(explrObject);
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showdetails(final JSONObject explrObject) throws JSONException {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.details, null, true);

        final TextView name = alertLayout.findViewById(R.id.mname);
        final TextView open = alertLayout.findViewById(R.id.mopen);
        final TextView rating = alertLayout.findViewById(R.id.mrating);
        final TextView number = alertLayout.findViewById(R.id.mnumber);
        final TextView website = alertLayout.findViewById(R.id.mwebsite);
        final TextView address = alertLayout.findViewById(R.id.maddress);
/*
        final ImageButton call = alertLayout.findViewById(R.id.call);
        final ImageButton webs = alertLayout.findViewById(R.id.web);
        final ImageButton direction = alertLayout.findViewById(R.id.direction);
*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        name.setText(explrObject.getString("name"));
        open.setText("open now - " + explrObject.getString("open"));
        number.setText(explrObject.getString("number"));
        website.setText(explrObject.getString("website"));
        address.setText(explrObject.getString("address"));

        if (!Objects.equals(explrObject.getString("rating"), "NA")) {
            rating.setText(explrObject.getString("rating") + "/5");
        } else {
            rating.setText(explrObject.getString("rating"));
        }

        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!Objects.equals(explrObject.getString("number"), "NA")) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + explrObject.getString("number")));
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            showSettingsAlert();
                            return;
                        }
                        startActivity(intent);
                    } else {
                        Toast.makeText(MapsActivity.this, "No contact detail available", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!Objects.equals(explrObject.getString("website"), "NA")) {
                        String url = explrObject.getString("website");
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else {
                        Toast.makeText(MapsActivity.this, "No website detail available", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = null;
                try {
                    gmmIntentUri = Uri.parse("google.navigation:q=" + explrObject.getString("lat") + "," + explrObject.getString("lng"));
                } catch (JSONException e) {
                    Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MapsActivity.this);

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
}
