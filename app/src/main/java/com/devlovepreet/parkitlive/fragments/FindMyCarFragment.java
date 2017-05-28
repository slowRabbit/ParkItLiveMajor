package com.devlovepreet.parkitlive.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devlovepreet.parkitlive.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import timber.log.Timber;


public class FindMyCarFragment extends Fragment implements OnMapReadyCallback {

    private static final String EXTRA_TEXT = "text";

    MapView mMapView;
    private GoogleMap map;
    private View rootView;

    public static FindMyCarFragment createFor(String text) {
        FindMyCarFragment fragment = new FindMyCarFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.fragment_find_my_car, container, false);
            MapsInitializer.initialize(this.getActivity());
            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);

        } catch (InflateException e) {
            Timber.e("Inflate Exception" + e);
        }
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

//        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String theme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));
        if (theme.equals(getResources().getString(R.string.pref_theme_light_value))) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_light));

        } else {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_dark));
        }

//        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json_dark));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(28.752091, 77.114194), 19));
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_icon_blue))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(28.752091, 77.114194)));

    }
}
