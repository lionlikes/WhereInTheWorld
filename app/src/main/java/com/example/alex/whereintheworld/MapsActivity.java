package com.example.alex.whereintheworld;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import com.google.android.gms.location.LocationServices;
import android.widget.Toast;



import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    Location mLastLocation;


    private double latDouble = 0.0;
    private double lngDouble = 0.0;

    double tlat;
    double tlong;

    private GoogleMap mMap;
    Button btnShowCoord;
    EditText edtAddress;
    TextView txtCoord;
    private Button bttnChangeMap;
    int mapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new mylocationListener();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Open GPS", Toast.LENGTH_LONG).show();
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnShowCoord = (Button)findViewById(R.id.btnShowCoordinates);
        edtAddress = (EditText)findViewById(R.id.edtAddress);
        txtCoord = (TextView)findViewById(R.id.txtCoordinates);

        btnShowCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetCoordinates().execute(edtAddress.getText().toString().replace(" ","+"));
            }
        });

        bttnChangeMap = findViewById(R.id.changeMapType);
        bttnChangeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                if(mapCount%4 == 0)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else if(mapCount%4 == 1)
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                else if(mapCount %4 == 2)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else if(mapCount%4 == 3)
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                else mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                mapCount++;

            }
        });


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    class mylocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (location!= null){
                tlat = location.getLatitude();
                tlong = location.getLongitude();
                updateMap();
            }
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


    private class GetCoordinates extends AsyncTask<String,Void,String> {
        ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Hold your horses....");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s",address);
                response = http.getHTTPData(url);
                return response;
            }
            catch (Exception ex)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                JSONObject jsonObject = new JSONObject(s);

                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                String somethingelse = ((JSONArray)jsonObject.get("results")).getJSONObject(0).get("formatted_address").toString();
                String something = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").get("location_type").toString();
                //String something = "beans";

                latDouble = Double.parseDouble(lat);
                lngDouble = Double.parseDouble(lng);

                updateMap();

                txtCoord.setText(String.format("Here in the world: %s | %s \nPrecision: %s\n\n%s",lat,lng,something,somethingelse));

                if(dialog.isShowing())
                    dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMap(){
        LatLng latlng = new LatLng(latDouble, lngDouble);
        mMap.addMarker(new MarkerOptions().position(latlng).title("New location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latDouble, lngDouble);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

    }
}
