package com.example.hopinnow.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.hopinnow.R;
import com.example.hopinnow.database.DriverDatabaseAccessor;
import com.example.hopinnow.database.UserDatabaseAccessor;
import com.example.hopinnow.entities.Car;
import com.example.hopinnow.entities.Driver;
import com.example.hopinnow.entities.Rider;
import com.example.hopinnow.helperclasses.ProgressbarDialog;
import com.example.hopinnow.statuslisteners.DriverProfileStatusListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Author: Hongru Qi
 * Version: 1.0.0
 * This is the main page for driver where is shows the map, online and menu button
 */
public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener, DriverProfileStatusListener {
    private GoogleMap mMap;
    MapFragment mapFragment;
    private LatLng edmonton = new LatLng(53.631611,-113.323975);
    private FloatingActionButton goOnline;
    private AutocompleteSupportFragment startUpAutoComplete;

    private Rider rider;
    private Driver driver;
    private LatLng pickUpLoc, startUpLoc, dropOffLoc;
    private String pickUpLocName, startUpLocName;
    private Marker pickUpMarker, dropOffMarker;
    private FloatingActionButton driverMenuBtn;
    private LatLng myPosition;
    private int currentRequestPageCounter = 0;

    private ProgressbarDialog progressbarDialog;
    private NavigationView navigationView;
    private DriverDatabaseAccessor userDatabaseAccessor;
    public static final String TAG = "DriverMenuActivity";
    private DrawerLayout drawerLayout;
    private TextView menuUserName;
    /**
     * set the visibility of goOnline button into invisible
     */
    public void setButtonInvisible(){
        goOnline.setVisibility(View.INVISIBLE);
    }

    /**
     * get the appear time of the fragment that display the current request
     * if it's the first time then display the pickup button
     * else display the emergency button and dropoff button
     * @return
     */
    public int getCurrentRequestPageCounter(){
        return this.currentRequestPageCounter;
    }
    public void setCurrentRequestPageCounter(int value){
        this.currentRequestPageCounter = value;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(DriverMapActivity.this);

        rider = new Rider();
        Car car = new Car("Auburn", "Speedster", "Cream", "111111");
        driver = new Driver("111@gmail.com", "12345678", "Lupin the Third",
                "12345678", 12.0, null, car, null);

        goOnline = findViewById(R.id.onlineBtn);
        goOnline.setOnClickListener(v -> {
            findViewById(R.id.onlineButtonText).setVisibility(View.INVISIBLE);
            switchFragment(R.layout.fragment_driver_requests);
        });
        startUpAutoComplete = ((AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.start_up_auto_complete));
        drawerLayout = findViewById(R.id.driver_drawer_layout);
        this.userDatabaseAccessor = new DriverDatabaseAccessor();
        navigationView = findViewById(R.id.nav_view_driver);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // a button listener
        driverMenuBtn = findViewById(R.id.driverMenuBtn);
        driverMenuBtn.setOnClickListener(v -> {
            menuUserName = findViewById(R.id.menuUserNameTextView);
            menuUserName.setText(driver.getName());
            // If the navigation drawer is not open then open it, if its already open then close it.
            drawerLayout.openDrawer(GravityCompat.START);
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 8.5f));
        pickUpMarker = mMap.addMarker(new MarkerOptions()
                .position(edmonton) //set to current location later on pickUpLoc
                .title("Edmonton")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker m) {  }

            @Override
            public void onMarkerDrag(Marker m) {
                LatLng newLatLng = m.getPosition();
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(newLatLng.latitude, newLatLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = Objects.requireNonNull(addresses).get(0).getAddressLine(0);
                startUpLoc = newLatLng;
                startUpLocName = address;
                startUpAutoComplete.setText(pickUpLocName);
            }

            @Override
            public void onMarkerDragEnd(Marker m) {
                LatLng newLatLng = m.getPosition();
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(newLatLng.latitude, newLatLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = Objects.requireNonNull(addresses).get(0).getAddressLine(0);
    }


    /**
     * we have a frame layout in the rider and driver activity
     * so that the fragments on it can switch easily by calling
     * switchFragment method,
     * @param caseId
     *      status of current program:
     */
    public void switchFragment(int caseId){
        FragmentManager t = getSupportFragmentManager();
        switch(caseId){
            // change the fragment to the one that display the current
            // request and the pickup user button
            case R.layout.fragment_driver_pick_rider_up:
                //t = getSupportFragmentManager().beginTransaction();
                t.beginTransaction().replace(R.id.fragment_place, new PickUpAndCurrentRequest())
                        .commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
                //change the fragment to the one that display the available list.
            case R.layout.fragment_driver_requests:
                //t = getSupportFragmentManager().beginTransaction();
                t.beginTransaction().replace(R.id.fragment_place, new RequestListFragment())
                        .commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case -1:
                FrameLayout fl = findViewById(R.id.fragment_place);
                fl.removeAllViews();
                goOnline.performClick();
                break;

        }
    }
    public void callNumber(String phoneNumber){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNumber));

        if (ActivityCompat.checkSelfPermission(DriverMapActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    public void clearMap(){
        mMap.clear();
    }
    public void updateBothMarker(){
        pickUpMarker = mMap.addMarker(new MarkerOptions()
                .position(pickUpLoc)
                .title("Pick Up Location")
                .visible(false)
                .icon(toBitmapMarkerIcon(getResources().getDrawable(R.drawable.marker_pick_up)))
                .draggable(true));

        dropOffMarker = mMap.addMarker(new MarkerOptions()
                .position(pickUpLoc)
                .title("Drop Off Location")
                .visible(false)
                .icon(toBitmapMarkerIcon(getResources().getDrawable(R.drawable.marker_drop_off)))
                .draggable(true));

    }

    public void setBothMarker(LatLng pickUpLoc, LatLng dropOffLoc){
        pickUpMarker.setVisible(true);
        pickUpMarker.setPosition(pickUpLoc);
        dropOffMarker.setVisible(true);
        dropOffMarker.setPosition(dropOffLoc);
        adjustMapFocus();
    }


    /**
     * set marker to map
     */
    public void setMapMarker(Marker m, LatLng latLng){
        if (m == null) {
            MarkerOptions opt = new MarkerOptions();

            opt.position(latLng);
            m = mMap.addMarker(opt);
        } else {
            m.setPosition(latLng);
        }
        adjustMapFocus();
    }

    /**
     * Sets up auto complete fragment from Google Places API.
     */
    private void setupAutoCompleteFragment() {
        assert startUpAutoComplete != null;
        startUpAutoComplete.setHint("Pick Up Location");
        startUpAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.ADDRESS, Place.Field.NAME,Place.Field.LAT_LNG));
        startUpAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                startUpLocName = place.getAddress();
                startUpLoc = place.getLatLng();
                setMapMarker(pickUpMarker,startUpLoc);
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e("An error occurred: ", status.toString());
            }
        });
        startUpAutoComplete.getView().findViewById(R.id.places_autocomplete_clear_button)
                .setOnClickListener(v -> {
                    startUpAutoComplete.setText("");
                    startUpLoc = null;
                    startUpLocName = null;
                    pickUpMarker.setVisible(false);
                });

    }
    /**
     * Transforms drawable into a bitmap drawable.
     * @param drawable
     *      information of the drawable to be turned into bitmap
     * @return
     *      Bitmap image for marker icon
     */
    private BitmapDescriptor toBitmapMarkerIcon(Drawable drawable) {
        //Stackoverflow post by Mohammed Haidar
        //https://stackoverflow.com/questions/35718103/
        //      how-to-specify-the-size-of-the-icon-on-the-marker-in-google-maps-v2-android
        //Answered by Rohit Bansal
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth()
                , drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    /**
     * adjust focus of the map according to the markers
     */


    public void adjustMapFocus(){
        LatLngBounds.Builder bound = new LatLngBounds.Builder();

        if ((pickUpLoc != null)&&(dropOffLoc != null)) {
            bound.include(pickUpLoc);
            bound.include(dropOffLoc);
        } else if (pickUpLoc != null) {
            bound.include(pickUpLoc);
        } else if (dropOffLoc != null) {
            bound.include(dropOffLoc);
        } else {
            return;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound.build(), 300));
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.driver_profile:
                Intent intent1 = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent1);
                break;
            case R.id.driver_trips:
                Intent intent2 = new Intent(getApplicationContext(), TripListActivity.class);
                startActivity(intent2);
                break;
            case R.id.car_info:
                Log.d(TAG, "Car info btn clicked!");
                userDatabaseAccessor.getDriverProfile(DriverMapActivity.this);
                break;
            case R.id.go_offline:
                Intent intent3 = new Intent(getApplicationContext(), DriverMapActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent3);
                this.overridePendingTransition(0, 0);
                finish();
                break;
            case R.id.driver_logout:
                userDatabaseAccessor.logoutUser();
                // go to the login activity again:
                Toast.makeText(getApplicationContext(),
                        "You are Logged out!", Toast.LENGTH_LONG).show();
                Intent intent4 = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent4);
                intent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onDriverProfileRetrieveSuccess(Driver driver) {
        // when retrieve the driver profile successful,
        // open vehicle view activity to display the car information
        Log.v(TAG, "Driver info retrieved!");
        Intent intent = new Intent(getApplicationContext(),  VehicleViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("DriverObject", driver);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDriverProfileRetrieveFailure() {

    }

    @Override
    public void onDriverProfileUpdateSuccess(Driver driver) {

    }

    @Override
    public void onDriverProfileUpdateFailure() {

    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        overridePendingTransition(0, 0);
//    }
}
