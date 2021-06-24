package hcmut.team15.emergencysupport.emergency;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
import hcmut.team15.emergencysupport.model.User;
import hcmut.team15.emergencysupport.profile.Profile;

public class VolunteerActivity extends AppCompatActivity {
    private EmergencyService emergencyService;
    private final ServiceConnection emergencyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emergencyService = ((EmergencyService.LocalBinder) service).getService();
            emergencyService.registerView(VolunteerActivity.this);
            reload();
            reloadMap();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            emergencyService.unregisterVolunteerActivity();
            emergencyService = null;
        }
    };
    private String caseId;
    private Case cs;
    private TextView name;
    private TextView phone;
    private TextView address;
    private TextView dateOfBirth;
    private TextView bloodType;
    private TextView anamnesis;
    private TextView allergens;
    private TextView distance;
    private Button acceptVolunteerButton;
    private Button stopVolunteerButton;

    private Location latestLocation;
    private Location victimLatestLocation;
    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private double lat, lng;
    private String addressLine;
    private String victimAddressLine;
    private Marker volunteerMarker;
    private Marker victimMarker;
    private TextView volunteerLocation;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
        name = findViewById(R.id.volunteer_victim_name_content);
        phone = findViewById(R.id.volunteer_victim_phone_content);
        address = findViewById(R.id.volunteer_victim_address_content);
        dateOfBirth = findViewById(R.id.volunteer_victim_dateofbirth_content);
        bloodType = findViewById(R.id.volunteer_victim_bloodtype_content);
        anamnesis = findViewById(R.id.volunteer_victim_anamnesis_content);
        allergens = findViewById(R.id.volunteer_victim_allergens_content);
        distance = findViewById(R.id.volunteer_victim_distance_content);
        acceptVolunteerButton = findViewById(R.id.volunteer_accept_btn);
        stopVolunteerButton = findViewById(R.id.volunteer_stop_btn);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.volunteer_map);
        android.location.Location loc = MainApplication.getInstance().getLocationService().getLastLocation();
        if (loc == null) {
            latestLocation = new Location(0, 0, 0);
        } else {
            latestLocation = new Location(loc);
        }
        String jsonCase = getIntent().getStringExtra("case");
        if (jsonCase != null) {
            try {
                Case cs = new Gson().fromJson(jsonCase, Case.class);
                User caller = cs.getCaller();
                if (caller != null) {
                    victimLatestLocation = caller.getCurrentLocation();
                }
            } catch (Exception ex) { }
        }
        geocoder = new Geocoder(this, Locale.getDefault());
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
                VolunteerActivity.this.googleMap = googleMap;
                if (victimLatestLocation == null || latestLocation == null) {
                    return;
                }
                prepareMarkers();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
                builder.include(new LatLng(victimLatestLocation.getLatitude(), victimLatestLocation.getLongitude()));
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 30);
                googleMap.animateCamera(cameraUpdate);
            }
        });
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCaseUpdate() {
        reload();
        reloadMap();
    }

    private void reload() {
        caseId = getIntent().getStringExtra("caseId");
        if (caseId == null) {
            return;
        }
        cs = emergencyService.getCase(caseId);

        Case acceptedCase = emergencyService.getAcceptedCase();
        Case callingCase = emergencyService.getCallingCase();

        if (callingCase != null) {
            acceptVolunteerButton.setText("Bạn đang phát tín hiệu khẩn cấp");
            acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang phát tín hiệu khẩn cấp rồi"));
            stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
        } else if (acceptedCase != null) {
            if (acceptedCase.getId().equals(caseId)) {
                acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp này");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang tham gia cuộc trợ giúp này"));
                stopVolunteerButton.setOnClickListener(v -> {
                    showStopVolunteerDialog();
                    reload();
                });
            } else {
                acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp khác");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang tình nguyện cho một người khác rồi"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
            }
        } else {
            if (cs != null) {
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
                acceptVolunteerButton.setText("Chấp nhận");
                acceptVolunteerButton.setOnClickListener(view -> {
                    emergencyService.acceptVolunteer(cs.getId());
                    notifyWithSnackbar("Cảm ơn bạn đã tham gia");
                    acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp này");
                    acceptVolunteerButton.setOnClickListener(v -> notifyWithSnackbar("Bạn đang tham gia cuộc trợ giúp này"));
                    stopVolunteerButton.setOnClickListener(v -> {
                        showStopVolunteerDialog();
                        reload();
                    });
                });
            } else {
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể tham gia. Có thể cuộc trợ giúp đã kết thúc"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
            }
        }

        if (cs != null) {
            User victim = cs.getCaller();
            Profile victimProfile = victim.getProfile();

            if (victimProfile.getName() != null && !victimProfile.getName().isEmpty()) {
                name.setText(victimProfile.getName());
            } else {
                name.setText(getString(R.string.volunteer_victim_name_content));
            }

            if (victimProfile.getAddress() != null && !victimProfile.getAddress().isEmpty()) {
                address.setText(victimProfile.getAddress());
            } else {
                address.setText(getString(R.string.volunteer_victim_address_content));
            }

            if (victimProfile.getPhone() != null && !victimProfile.getPhone().isEmpty()) {
                phone.setText(victimProfile.getPhone());
            } else {
                phone.setText(getString(R.string.volunteer_victim_phone_content));
            }

            if (victimProfile.getDateOfBirth() != null && !victimProfile.getDateOfBirth().isEmpty()) {
                dateOfBirth.setText(victimProfile.getDateOfBirth());
            } else {
                dateOfBirth.setText(getString(R.string.volunteer_victim_dateofbirth_content));
            }

            if (victimProfile.getBloodType() != null && !victimProfile.getBloodType().isEmpty()) {
                bloodType.setText(victimProfile.getBloodType());
            } else {
                bloodType.setText(getString(R.string.volunteer_victim_bloodtype_content));
            }

            if (victimProfile.getAllergens() != null && !victimProfile.getAllergens().isEmpty()) {
                allergens.setText(victimProfile.getAllergens());
            } else {
                allergens.setText(getString(R.string.volunteer_victim_allergens_content));
            }
        }
    }

    private void reloadMap() {
        if (googleMap == null) {
            return;
        }

        boolean shouldAnimateCamera = false;

        if (cs != null) {
            Location newVictimLocation = cs.getCaller().getCurrentLocation();
            Location newVolunteerLocation = MainApplication.getInstance().getLocationService().getCustomLastLocation();

            if (latestLocation == null) {
                shouldAnimateCamera = true;
            } else {
                if (newVolunteerLocation != null && Location.distanceBetween(latestLocation, newVolunteerLocation) > 1000) {
                    shouldAnimateCamera = true;
                }
            }

            if (victimLatestLocation == null) {
                shouldAnimateCamera = true;
            } else {
                if (newVictimLocation != null && Location.distanceBetween(victimLatestLocation, newVictimLocation) > 1000) {
                    shouldAnimateCamera = true;
                }
            }

            if (newVolunteerLocation != null) {
                latestLocation = newVolunteerLocation;
            }
            if (newVictimLocation != null) {
                victimLatestLocation = newVictimLocation;
            }
            if (victimLatestLocation != null && latestLocation != null) {
                String d = String.format(Locale.getDefault(), "%.1f", Location.distanceBetween(latestLocation, victimLatestLocation)) + "m";
                distance.setText(d);
            }
        }

        prepareMarkers();

        if (shouldAnimateCamera) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (latestLocation != null) {
                builder.include(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
            }
            if (victimLatestLocation != null) {
                builder.include(new LatLng(victimLatestLocation.getLatitude(), victimLatestLocation.getLongitude()));
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 30);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    private void prepareMarkers() {
        if (latestLocation != null) {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latestLocation.getLatitude(), latestLocation.getLongitude(), 1
                );
                addressLine = addresses.get(0).getAddressLine(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (volunteerMarker != null) {
                volunteerMarker.remove();
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()))
                    .title("Bạn ở đây")
                    .snippet(addressLine);

            volunteerMarker = googleMap.addMarker(markerOptions);
            if (volunteerMarker != null) {
                volunteerMarker.showInfoWindow();
            }
        }

        if (victimLatestLocation != null) {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        victimLatestLocation.getLatitude(), victimLatestLocation.getLongitude(), 1
                );
                victimAddressLine = addresses.get(0).getAddressLine(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (victimMarker != null) {
                victimMarker.remove();
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(victimLatestLocation.getLatitude(), victimLatestLocation.getLongitude()))
                    .title("Họ ở đây")
                    .snippet(victimAddressLine);

            victimMarker = googleMap.addMarker(markerOptions);
            if (victimMarker != null) {
                victimMarker.showInfoWindow();
            }
        }
    }

    private void showStopVolunteerDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Xác nhận dừng trợ giúp");
        alertDialogBuilder.setMessage("Bạn có chắc muốn dừng trợ giúp?");
        alertDialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
            emergencyService.stopVolunteer();
            reload();
            notifyWithSnackbar("Cảm ơn bạn đã tham gia");
        });
        alertDialogBuilder.setNegativeButton("Tiếp tục trợ giúp", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void notifyWithSnackbar(String message) {
        Snackbar sb = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        sb.show();
    }
}
