package com.example.magelanapp;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.magelanapp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Date dateStart, dateEnd;

    private UserLocation mUserLocation;

    FirebaseAuth auth;
    FirebaseFirestore db;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("User Locations");

        Intent intent = getIntent();
        String start = intent.getStringExtra("datumPocetka") + " " + intent.getStringExtra("vremePocetka");
        String end = intent.getStringExtra("datumKraja") + " " + intent.getStringExtra("vremeKraja");

        System.out.println("DATUM :" + start);
        System.out.println("DATUM :" + end);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            dateStart = dateFormat.parse(start);
            dateEnd = dateFormat.parse(end);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        //prikazivanje lokacija za izabrani vremenski period na mapi
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String originalString = document.getId();
                        int firstIndex = originalString.indexOf(" ");

                        String substring = originalString.substring(0, firstIndex);

                        auth = FirebaseAuth.getInstance();
                        String user = auth.getCurrentUser().getEmail();

                        if (substring.equals(user)) {

                            mUserLocation = new UserLocation();

                            double lat = document.getGeoPoint("geo_point").getLatitude();
                            double lng = document.getGeoPoint("geo_point").getLongitude();

                            LatLng latLng = new LatLng(lat, lng);

                            Date date = document.getDate("timestamp");

                            int resultStart = date.compareTo(dateStart);
                            int resultEnd = date.compareTo(dateEnd);

                            if (resultStart > 0 & resultEnd < 0) {

                                Log.d(TAG, "IZABRANI DATUM " + date);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(date.toString()));

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 8);
                                mMap.animateCamera(cameraUpdate);
                            }

                        }
                    }
                }
            }
        });

    }

}