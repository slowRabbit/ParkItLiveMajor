package com.devlovepreet.parkitlive.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.devlovepreet.parkitlive.AppController;
import com.devlovepreet.parkitlive.GoogleMapsFiles.DirectionsJSONParser;
import com.devlovepreet.parkitlive.R;
import com.devlovepreet.parkitlive.data.AppConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FindParkingFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String EXTRA_TEXT = "text";
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    PolylineOptions lineOptions = null;
    String MY_PREFS_NAME="sharedpreference_for_saving_latitude_and_longitude";

    Marker markerInitial, marker1, marker2, marker3, marker4, marker5, marker6, marker7, marker8, marker9, marker10, markerSelected;
    LatLng latLngInitial, latLng1, latLng2, latLng3, latLng4, latLng5, latLng6, latLng7, latLng8, latLng9, latLng10, latLngSelected;
    LatLng latLngG3SDefault, latLngLastClicked;
    Marker tempMarker1;

    int parkingStatus1=0, parkingStatus2=0, parkingStatus3=0, parkingStatus4=0, parkingStatus5=0, parkingStatus6=0, parkingStatus7=0, parkingStatus8=0, parkingStatus9=0, parkingStatus10=0;
    int gasStatus=0;

    Handler handler;
    Timer timer;
    TimerTask doAsynchronousTask;
    int time=5000;

    private ProgressDialog pDialog;


    String OCCUPIED="occupied", EMPTY="empty";

    public static FindParkingFragment createFor(String text) {
        FindParkingFragment fragment = new FindParkingFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();
    }



    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        callAsynchronousTask();

    }

    public void callAsynchronousTask()
    {

        handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getParkingStatus();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, time); //execute in every 50000 ms



    }

    private void setUpMapIfNeeded() {

        if (mGoogleMap == null) {
            getMapAsync(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;


        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("latitude", "28.733898");
        editor.putString("longitude", "77.113388");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String theme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        addAllDynamicMarkers();
        addAllStaticMarkers();
        setOnClickListenerOnMap();

//        if (theme.equals(getResources().getString(R.string.pref_theme_light_value))) {
//            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_light));
//
//        } else {
//            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_dark));
//        }

        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_light));

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        latLngG3SDefault=new LatLng(28.733898, 77.113388);
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLngG3SDefault, 17.0f);
        //mGoogleMap.moveCamera( cu );
        mGoogleMap.animateCamera(cu);

    }
    

    public void addAllDynamicMarkers()
    {
        latLng1=new LatLng(28.732713, 77.113843);
        latLng2=new LatLng(28.733052, 77.113130);
        latLng3=new LatLng(28.733188, 77.112942);
        latLng4=new LatLng(28.734004, 77.112161);
        latLng5=new LatLng(28.734079, 77.112044);
        latLng6=new LatLng(28.733200, 77.114169);
        latLng7=new LatLng(28.733421, 77.113996);
        latLng8=new LatLng(28.733765, 77.113056);
        latLng9=new LatLng(28.734141, 77.112376);
        latLng10=new LatLng(28.734296, 77.112505);

        marker1=mGoogleMap.addMarker(new MarkerOptions().position(latLng1).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker2=mGoogleMap.addMarker(new MarkerOptions().position(latLng2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker3=mGoogleMap.addMarker(new MarkerOptions().position(latLng3).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker4=mGoogleMap.addMarker(new MarkerOptions().position(latLng4).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker5=mGoogleMap.addMarker(new MarkerOptions().position(latLng5).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker6=mGoogleMap.addMarker(new MarkerOptions().position(latLng6).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker7=mGoogleMap.addMarker(new MarkerOptions().position(latLng7).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker8=mGoogleMap.addMarker(new MarkerOptions().position(latLng8).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker9=mGoogleMap.addMarker(new MarkerOptions().position(latLng9).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));
        marker10=mGoogleMap.addMarker(new MarkerOptions().position(latLng10).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(EMPTY));

    }

    public void addAllStaticMarkers()
    {
        latLngInitial=new LatLng(28.733754, 77.114063);
        markerInitial=mGoogleMap.addMarker(new MarkerOptions().position(latLngInitial).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(OCCUPIED));

        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.732463, 77.113606)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.732773, 77.113073)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733036, 77.112762)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733726, 77.111780)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733585, 77.111973)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733728, 77.112129)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733846, 77.111941)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733511, 77.112271)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733553, 77.112606)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733384, 77.112472)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.734513, 77.112842)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.734701, 77.112660)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.735223, 77.113266)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.734931, 77.113057)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.734127, 77.114843)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733859, 77.114591)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733429, 77.114439)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733589, 77.114243)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733323, 77.114013)));
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(OCCUPIED).position(new LatLng(28.733191, 77.114260)));

    }

    public void setOnClickListenerOnMap()
    {
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker destinationMarker) {

                //check if a red marker is clicked, i.e a marker with title = occupied
                String title=destinationMarker.getTitle();
                if(title.contentEquals(OCCUPIED))
                    return false;

                LatLng latLngDestination = destinationMarker.getPosition();

                deleteAllExistingPathFromMap();
                //addAllDynamicMarkers();
                setMarkerAccordingToApiResponse();
                addAllStaticMarkers();

                destinationMarker.remove();
                tempMarker1=mGoogleMap.addMarker(new MarkerOptions().position(latLngDestination).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title("Destination"));

                //---
                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(latLngInitial, latLngDestination);
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
                //------

                return false;
            }
        });

    }

    public int checkMarkerNumberClicked(LatLng latLng)
    {
        if(latLng==latLng1)
            return 1;
        else if(latLng==latLng2)
            return 2;
        else if(latLng==latLng3)
            return 3;
        else if(latLng==latLng4)
            return 4;
        else if(latLng==latLng5)
            return 5;
        else if(latLng==latLng6)
            return 6;
        else if(latLng==latLng7)
            return 7;
        else if(latLng==latLng8)
            return 8;
        else if(latLng==latLng9)
            return 9;
        else if(latLng==latLng10)
            return 10;
        return 1;
    }

    public void deleteAllExistingPathFromMap()
    {
        mGoogleMap.clear();
    }



    private void getParkingStatus() {
        // Tag used to cancel the request
        String tag_json_obj = "parking_status_request";

        AppConfig appConfig = new AppConfig();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, appConfig.PARKING_STATUS, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject main) {



                        Log.d("parkingstatus", main.toString());
                        try {

                            JSONObject response=main.getJSONObject("msg");
                            parkingStatus1=response.getInt("parking1");
                            parkingStatus2=response.getInt("parking2");
                            parkingStatus3=response.getInt("parking3");
                            parkingStatus4=response.getInt("parking4");
                            parkingStatus5=response.getInt("parking5");
                            parkingStatus6=response.getInt("parking6");
                            parkingStatus7=response.getInt("parking7");
                            parkingStatus8=response.getInt("parking8");
                            parkingStatus9=response.getInt("parking9");
                            parkingStatus10=response.getInt("parking10");
                            gasStatus=response.getInt("gas");

                           // Log.i("parkingstatus", "status : "+parkingStatus1+parkingStatus2+gasStatus);

                            setMarkerAccordingToApiResponse();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("parkingstatus", "Error: " + error.getMessage());
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_some_error_occurred), Toast.LENGTH_LONG).show();

            }
        }) {

//            @Override
//            public byte[] getBody() {
//              needed in post requests
//
//  HashMap<String, String> params = new HashMap<String, String>();
//                params.put(getResources().getString(R.string.key_email), email);
//                params.put(getResources().getString(R.string.key_password), password);
//                return new JSONObject(params).toString().getBytes();
//            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "JWT eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJfaWQiOiI1OTI2Y2E1NmU1OWZjZDU4ZTIzMDJjZDgiLCJlbWFpbCI6ImRldi5sb3ZlcHJlZXRzaW5naEBnbWFpbC5jb20iLCJwYXNzd29yZCI6IiQyYSQxMCRVU1p2bUswU3dpWGZiRkhWLkRJeHllUnV1Si5sUTE0cVpWUFpTcVRSL0VoVXBFWS9qQ043eSIsIm5hbWUiOiJMb3ZlcHJlZXQgU2luZ2giLCJ2ZWhpY2xlTnVtYmVyIjoiTUQgMTEwMiIsIl9fdiI6MCwibWludXRlcyI6MCwiaG91ciI6MCwiZGF5IjowLCJtb250aCI6MCwieWVhciI6MCwicGFya1N0YXJ0RmxhZyI6MH0.gbam6xKhAok3rf9ceixSgYTYh48zfXviecIAPXOjpj8");
                return headers;
            }

        };

        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

//    private void showDialog() {
//        if (!pDialog.isShowing())
//            pDialog.show();
//    }
//
//    private void hideDialog() {
//        if (pDialog.isShowing())
//            pDialog.dismiss();
//    }

    public void setMarkerAccordingToApiResponse()
    {


        Log.d("parkingstatus", "Marker updated");

        if(parkingStatus1==0)
            updateMarker(marker1, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker1, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus2==0)
            updateMarker(marker2, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker2, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus3==0)
            updateMarker(marker3, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker3, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus4==0)
            updateMarker(marker4, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker4, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus5==0)
            updateMarker(marker5, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker5, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus6==0)
            updateMarker(marker6, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker6, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus7==0)
            updateMarker(marker7, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker7, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus8==0)
            updateMarker(marker8, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker8, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus9==0)
            updateMarker(marker9, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker9, BitmapDescriptorFactory.HUE_RED);

        if(parkingStatus10==0)
            updateMarker(marker10, BitmapDescriptorFactory.HUE_GREEN);
        else
            updateMarker(marker10, BitmapDescriptorFactory.HUE_RED);


    }

    public void updateMarker(Marker marker, float color)
    {
        //Log.d("parkingstatus", "update marker called");

       // marker.remove();
        LatLng latLngMarker=marker.getPosition();
        marker.remove();
        marker=mGoogleMap.addMarker(new MarkerOptions().position(latLngMarker).icon(BitmapDescriptorFactory.defaultMarker(color)));

        if(color==BitmapDescriptorFactory.HUE_GREEN)
            marker.setTitle(EMPTY);
        else
            marker.setTitle(OCCUPIED);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(200);
        mLocationRequest.setFastestInterval(200);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
       // mCurrLocationMarker.setPosition(latLng);

        //Place current location marker

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_icon_blue))
                .anchor(0.0f, 1.0f); // Anchors the marker on the bottom left;
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);


        //now we will recompute the path between current location and specified location
        //Navigator navigator = new Navigator(mGoogleMap, new LatLng(location.getLatitude(), location.getLongitude()), latLng1);
        //navigator.findDirections(false);

        //move map camera
       // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 200));
       // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
           // Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            //PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.GREEN);
            }

            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);
        }
    }


}
