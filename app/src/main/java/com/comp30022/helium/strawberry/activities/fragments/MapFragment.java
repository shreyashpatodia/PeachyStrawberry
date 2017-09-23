package com.comp30022.helium.strawberry.activities.fragments;

import android.location.Location;
import android.os.Bundle;

import com.comp30022.helium.strawberry.components.server.PeachServerInterface;
import com.comp30022.helium.strawberry.entities.User;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.helium.strawberry.R;
import com.comp30022.helium.strawberry.components.location.LocationEvent;
import com.comp30022.helium.strawberry.components.location.LocationServiceFragment;
import com.comp30022.helium.strawberry.components.map.StrawberryMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends LocationServiceFragment implements OnMapReadyCallback {
    private static final String TAG = MapFragment.class.getSimpleName();
    private StrawberryMap map;
    private SupportMapFragment mMapView;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        //mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = new StrawberryMap(googleMap);

        // TODO: update with real friend object
        User friend = new User("testid", "testuser");

        // init locations
        Location friendLoc = locationService.getUserLocation(friend);
        map.updateMarker("friendLocation", "Friend Location", friendLoc);

        Location currentLocation = locationService.getDeviceLocation();
        map.updateMarker("currentLocation", "You are here", currentLocation);

        // move camera
        List<Location> locations = new ArrayList<>();
        locations.add(friendLoc);
        locations.add(currentLocation);
        map.moveCamera(locations, 200);
    }

    @Override
    public void update(LocationEvent updatedLocation) {
        if (map == null) {
            Log.e(TAG, "Map has not been initialized yet, ditching new location update");
            return;
        }

        User user = updatedLocation.getKey();
        Location currentLocation = updatedLocation.getValue();


        if(user.equals(PeachServerInterface.currentUser())) {
            map.updateMarker("currentLocation", "You are here", currentLocation);
        } else {
            map.updateMarker("friendLocation", "Friend location", currentLocation);
        }

        map.updatePath("currentLocation", "friendLocation");
    }

    @Override
    protected void onResumeAction() {
        mMapView.onResume();
    }

    @Override
    protected void onPauseAction() {
        mMapView.onPause();
    }
}
