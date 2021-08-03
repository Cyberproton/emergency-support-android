package hcmut.team15.emergencysupport.emergency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.location.LocationService;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
import hcmut.team15.emergencysupport.model.User;
import hcmut.team15.emergencysupport.notificationCard.Notification;
import hcmut.team15.emergencysupport.notificationCard.notificationAdapter;

public class NotifyFromVolunteerActivity extends AppCompatActivity {
    private static boolean started = false;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Notification> exampleList;
    private List<User> volunteers = new ArrayList<>();
    private Case cs;
    private Location latestLocation;

    private EmergencyService emergencyService;
    private ServiceConnection emergencyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emergencyService = ((EmergencyService.LocalBinder) service).getService();
            emergencyService.registerView(NotifyFromVolunteerActivity.this);
            emergencyService.startEmergency();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            emergencyService.stopEmergency();
            emergencyService.unregisterView(NotifyFromVolunteerActivity.this);
            emergencyService = null;
        }
    };

    private LocationService locationService;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService = ((LocationService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
        }
    };

    //Google-map
    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private double lat, lng;
    private String addressLine;
    private Marker marker;
    private TextView userLocation;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_from_volunteer);

        if (getIntent().getBooleanExtra("startFromButton", false) && started) {
            return;
        }

        exampleList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new notificationAdapter(exampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vui lòng xác nhận");
        builder.setMessage("Bạn có chắc món dừng phát tín hiệu?");
        builder.setPositiveButton("Dừng phát", (dialog, which) -> {
            stopEmergency();
        });
        builder.setNegativeButton("Tiếp tục", (dialog, which) -> {
            dialog.cancel();
        });
        alertDialog = builder.create();
    }

    @Override
    protected void onStart() {
        Log.d(getClass().getSimpleName(), "Activity Started");
        super.onStart();

        boolean startFromButton = getIntent().getBooleanExtra("startFromButton", false);

        if (started) {
            finish();
            return;
        }

        if (startFromButton) {
            Toast.makeText(this, "Nhận tín hiệu từ feed, bắt đầu phát tín hiệu", Toast.LENGTH_LONG).show();
        }

        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
        android.location.Location loc = MainApplication.getInstance().getLocationService().getLastLocation();
        MainApplication.getInstance().registerNotifyFromVolunteerActivity(this);

        if (loc == null) {
            latestLocation = new Location(0, 0, 0);
        } else {
            latestLocation = new Location(loc);
        }
        Log.d("latest loc", latestLocation.getLatitude() + " " + latestLocation.getLongitude());

        //Google-map
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.volunteer_map);
        userLocation = findViewById(R.id.user_location_label);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        try {
                            geocoder = new Geocoder(NotifyFromVolunteerActivity.this
                                    , Locale.getDefault());

                            List<Address> addresses = geocoder.getFromLocation(
                                    latestLocation.getLatitude(), latestLocation.getLongitude(), 1
                            );
                            Log.d("addresses", addresses.size() + "");
                            lat = addresses.get(0).getLatitude();
                            //lng = addresses.get(0).getLongitude();
                            addressLine = addresses.get(0).getAddressLine(0);
                            //addressLine = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.d("addressLine", "" + addressLine);

                        if (latestLocation.isNull() || addressLine == null) {
                            userLocation.setText("Dịch vụ vị trí có thể đang gặp trục trặc");
                        } else {
                            userLocation.setText("Tôi đang đứng ở: " + addressLine);
                        }
                        LatLng latLng = new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude());
                        MarkerOptions options = new MarkerOptions().position(latLng).title("Bạn ở đây");
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        marker = googleMap.addMarker(options);
                    }
                });
                NotifyFromVolunteerActivity.this.googleMap = googleMap;
            }
        });

        started = true;
    }

    @Override
    protected void onStop() {
        Log.d(getClass().getSimpleName(), "Activity Stopped");
        unbindService(emergencyServiceConnection);
        started = false;
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void insertItem(User volunteer, float distance) {
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, volunteer.getUsername(), String.format("%.2f", distance) + "m"));
        mAdapter.notifyDataSetChanged();
    }

    public void removeItem(User volunteer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exampleList.removeIf(n -> n.getText1().equals(volunteer.getUsername()));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void insertItem(View view) {
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, "test", "0123456789"));
        mAdapter.notifyDataSetChanged();
    }

    public void stopSignal(View view) {
        showDialog();
    }

    public void updateCase(Case updated) {
        cs = updated;
        Location loc = cs.getCaller().getCurrentLocation();
        if (loc != null) {
            latestLocation = loc;
        }
        updatePosition();
        exampleList.clear();
        for (User volunteer : cs.getVolunteers()) {
            float distance = Location.distanceBetween(cs.getCaller().getCurrentLocation(), volunteer.getCurrentLocation());
            exampleList.add(new Notification(R.drawable.ic_baseline_person_24, volunteer.getUsername(), String.format("%.2f", distance) + "m"));
        }
        mAdapter.notifyDataSetChanged();
    }

    public void updatePosition() {
        if (googleMap == null || latestLocation == null) {
            return;
        }
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latestLocation.getLatitude(), latestLocation.getLongitude(), 1
            );

            //không có lỗi
            addressLine = addresses.get(0).getAddressLine(0);
            Log.d("updatepos", addressLine);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        LatLng latLng = new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude());
        MarkerOptions options = new MarkerOptions().position(latLng).title("Bạn ở đây");
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(options);
        if (latestLocation.isNull() || addressLine == null) {
            userLocation.setText("Dịch vụ vị trí có thể đang gặp trục trặc");
        } else {
            userLocation.setText("Tôi đang đứng ở: " + addressLine);
        }
    }

    public void onCaseClosed() {
        CharSequence message = "Tình nguyện viên đã rời khỏi cuộc trợ giúp";
        Snackbar sb = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        sb.show();
    }

    public void stopEmergency() {
        if (emergencyService != null) {
            emergencyService.stopEmergency();
        }
        Intent intent = new Intent(NotifyFromVolunteerActivity.this, EmergencyActivity.class);
        startActivity(intent);
        MainApplication.getInstance().unregisterNotifyFromVolunteerActivity();
        finish();
    }

    private void showDialog() {
        alertDialog.show();
    }

    public static boolean isStarted() {
        return started;
    }
}