package com.example.sep490_mobile.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sep490_mobile.CustomPlace;
import com.example.sep490_mobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 13f;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private int selectedRadius = 5000; // default 5km

    private List<CustomPlace> allPlaces = new ArrayList<>();

    // UI
    private EditText editTextSearch;
    private Button buttonSearch, buttonCurrentLocation;
    private RadioGroup radioGroupRadius;

    private DatabaseReference placesRef;
    private ValueEventListener placesValueEventListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        buttonCurrentLocation = view.findViewById(R.id.buttonCurrentLocation);
        radioGroupRadius = view.findViewById(R.id.radioGroupRadius);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize Firebase Database reference
        placesRef = FirebaseDatabase.getInstance().getReference("customPlaces");

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up listeners
        buttonSearch.setOnClickListener(v -> searchAddressAndShowNearbyPlaces(editTextSearch.getText().toString().trim()));
        radioGroupRadius.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio5km) {
                selectedRadius = 5000;
            } else if (checkedId == R.id.radio10km) {
                selectedRadius = 10000;
            }
            filterAndShowPlaces();
        });
        buttonCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        // Load places from Firebase
        loadPlacesFromFirebase();
    }

    private void loadPlacesFromFirebase() {
        placesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if fragment is still added before processing data
                if (!isAdded()) {
                    return;
                }
                allPlaces.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CustomPlace place = child.getValue(CustomPlace.class);
                    // Assuming CustomPlace has isApproved and isLocked fields
                    if (place != null && place.isApproved && !place.isLocked) {
                        allPlaces.add(place);
                    }
                }
                filterAndShowPlaces();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        placesRef.addValueEventListener(placesValueEventListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        getCurrentLocation();

        mMap.setOnMarkerClickListener(marker -> {
            LatLng pos = marker.getPosition();
            String label = marker.getTitle();
            if (label != null && label.equals("Vị trí của bạn")) {
                return false;
            }
            openGoogleMapsDirections(pos.latitude, pos.longitude, label);
            return true;
        });
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (!isAdded()) return;
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                            filterAndShowPlaces();
                        }
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (!isAdded()) return;
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                        filterAndShowPlaces();
                    }
                }
            });
        } catch (SecurityException e) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Không có quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void filterAndShowPlaces() {
        // *** FIX: Check if the fragment is attached to a context before proceeding ***
        if (!isAdded() || mMap == null) {
            return;
        }
        mMap.clear();

        LatLng center = userLocation != null ? userLocation : new LatLng(10.0452, 105.7469); // Default to Can Tho

        mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(selectedRadius)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(3f));

        if (userLocation != null) {
            Bitmap myPosIcon = createMarkerIconWithText(requireContext(), "Vị trí của bạn");
            mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Vị trí của bạn")
                    .icon(BitmapDescriptorFactory.fromBitmap(myPosIcon))
                    .anchor(0.1f, 1f));
        }

        for (CustomPlace place : allPlaces) {
            double dist = distance(center.latitude, center.longitude, place.lat, place.lng);
            if (dist <= selectedRadius) {
                Bitmap icon = createMarkerIconWithText(requireContext(), place.name);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(place.lat, place.lng))
                        .title(place.name)
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        .anchor(0.1f, 1f));
            }
        }
    }

    private void searchAddressAndShowNearbyPlaces(String addressStr) {
        if (!isAdded() || mMap == null) return;
        if (addressStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập địa chỉ cần tìm!", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy địa chỉ!", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            mMap.clear();

            Bitmap searchIcon = createMarkerIconWithText(requireContext(), addressStr);
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(addressStr)
                    .icon(BitmapDescriptorFactory.fromBitmap(searchIcon))
                    .anchor(0.1f, 1f));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(selectedRadius)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(3f));

            for (CustomPlace place : allPlaces) {
                double dist = distance(latLng.latitude, latLng.longitude, place.lat, place.lng);
                if (dist <= selectedRadius) {
                    Bitmap icon = createMarkerIconWithText(requireContext(), place.name);
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(place.lat, place.lng))
                            .title(place.name)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .anchor(0.1f, 1f));
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi tìm địa chỉ!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        if (!isAdded()) return false;
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Cần quyền truy cập vị trí để tiếp tục", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, result);
        return result[0];
    }

    public Bitmap createMarkerIconWithText(Context context, String text) {
        int width = Math.max(250, (int)(text.length() * 26));
        int height = 90;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_red);
        if (markerDrawable != null) {
            markerDrawable.setBounds(0, 20, 60, 90);
            markerDrawable.draw(canvas);
        } else {
            Paint paintCircle = new Paint();
            paintCircle.setColor(Color.RED);
            paintCircle.setStyle(Paint.Style.FILL);
            canvas.drawCircle(30, 60, 30, paintCircle);
        }

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        float rectLeft = 65;
        float rectTop = 10;
        float rectRight = width - 10;
        float rectBottom = 70;
        float radius = 25;
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, radius, radius, bgPaint);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setAntiAlias(true);
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, radius, radius, borderPaint);

        Paint paint = new Paint();
        paint.setTextSize(36);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setAntiAlias(true);
        canvas.drawText(text, 80, 55, paint);

        return bitmap;
    }

    public void openGoogleMapsDirections(double lat, double lng, String label) {
        if (!isAdded()) return;
        String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng + " (" + label + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // It's a good practice to remove the listener when the view is destroyed
        if (placesRef != null && placesValueEventListener != null) {
            placesRef.removeEventListener(placesValueEventListener);
        }
        // Stop location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}