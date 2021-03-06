package hcmut.team15.emergencysupport.emergency;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
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
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.call.CallActivity;
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
    private TextView username;
    private TextView usernameLabel;
    private TextView name;
    private TextView nameLabel;
    private TextView phone;
    private TextView phoneLabel;
    private TextView address;
    private TextView addressLabel;
    private TextView dateOfBirth;
    private TextView dateOfBirthLabel;
    private TextView bloodType;
    private TextView bloodTypeLabel;
    private TextView allergens;
    private TextView allergensLabel;
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
        username = findViewById(R.id.volunteer_victim_username_content);
        usernameLabel = findViewById(R.id.volunteer_victim_username_label);
        name = findViewById(R.id.volunteer_victim_name_content);
        nameLabel = findViewById(R.id.volunteer_victim_name_label);
        phone = findViewById(R.id.volunteer_victim_phone_content);
        phoneLabel = findViewById(R.id.volunteer_victim_phone_label);
        address = findViewById(R.id.volunteer_victim_address_content);
        addressLabel = findViewById(R.id.volunteer_victim_address_label);
        dateOfBirth = findViewById(R.id.volunteer_victim_dateofbirth_content);
        dateOfBirthLabel = findViewById(R.id.volunteer_victim_dateofbirth_label);
        bloodType = findViewById(R.id.volunteer_victim_bloodtype_content);
        bloodTypeLabel = findViewById(R.id.volunteer_victim_bloodtype_label);
        allergens = findViewById(R.id.volunteer_victim_allergens_content);
        allergensLabel = findViewById(R.id.volunteer_victim_allergens_label);
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
                googleMap.setOnMapLoadedCallback(() -> {
                    prepareMarkers();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
                    builder.include(new LatLng(victimLatestLocation.getLatitude(), victimLatestLocation.getLongitude()));
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 30);
                    googleMap.animateCamera(cameraUpdate);
                });
            }
        });
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCaseClosed() {
        Intent intent = new Intent(this, CallActivity.class);
        startActivity(intent);
        finish();
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
        Log.w(getClass().getSimpleName(), "Case id: " + caseId);
        for (Map.Entry<String, Case> entry : emergencyService.getCases().entrySet()) {
            Log.i(getClass().getSimpleName(), "Available: " + entry.getKey() + " " + entry.getValue().getId());
        }
        cs = emergencyService.getCase(caseId);

        if (cs == null) {
            Log.w(getClass().getSimpleName(), "Case is null");
        }

        Case acceptedCase = emergencyService.getAcceptedCase();
        Case callingCase = emergencyService.getCallingCase();

        if (callingCase != null) {
            acceptVolunteerButton.setText("B???n ??ang ph??t t??n hi???u kh???n c???p");
            acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("B???n ??ang ph??t t??n hi???u kh???n c???p r???i"));
            stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Kh??ng th??? k???t th??c cu???c tr??? gi??p b???n ch??a tham gia"));
        } else if (acceptedCase != null) {
            if (acceptedCase.getId().equals(caseId)) {
                acceptVolunteerButton.setText("B???n ??ang tham gia cu???c tr??? gi??p n??y");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("B???n ??ang tham gia cu???c tr??? gi??p n??y"));
                stopVolunteerButton.setOnClickListener(v -> {
                    showStopVolunteerDialog();
                    reload();
                });
            } else {
                acceptVolunteerButton.setText("B???n ??ang tham gia cu???c tr??? gi??p kh??c");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("B???n ??ang t??nh nguy???n cho m???t ng?????i kh??c r???i"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Kh??ng th??? k???t th??c cu???c tr??? gi??p b???n ch??a tham gia"));
            }
        } else {
            if (cs != null) {
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Kh??ng th??? k???t th??c cu???c tr??? gi??p b???n ch??a tham gia"));
                acceptVolunteerButton.setText("Ch???p nh???n");
                acceptVolunteerButton.setOnClickListener(view -> {
                    emergencyService.acceptVolunteer(cs.getId());
                    notifyWithSnackbar("C???m ??n b???n ???? tham gia");
                    acceptVolunteerButton.setText("B???n ??ang tham gia cu???c tr??? gi??p n??y");
                    acceptVolunteerButton.setOnClickListener(v -> notifyWithSnackbar("B???n ??ang tham gia cu???c tr??? gi??p n??y"));
                    stopVolunteerButton.setOnClickListener(v -> {
                        showStopVolunteerDialog();
                        reload();
                    });
                });
            } else {
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Kh??ng th??? tham gia. C?? th??? cu???c tr??? gi??p ???? k???t th??c"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Kh??ng th??? k???t th??c cu???c tr??? gi??p b???n ch??a tham gia"));
            }
        }

        if (cs != null) {
            User victim = cs.getCaller();
            Profile victimProfile = victim.getProfile();

            username.setText(victim.getUsername());

            if (victimProfile != null && victimProfile.getName() != null && !victimProfile.getName().isEmpty()) {
                nameLabel.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
                name.setText(victimProfile.getName());
            } else {
                nameLabel.setVisibility(View.GONE);
                name.setVisibility(View.GONE);
                name.setText(getString(R.string.volunteer_victim_name_content));
            }

            if (victimProfile != null && victimProfile.getAddress() != null && !victimProfile.getAddress().isEmpty()) {
                addressLabel.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);
                address.setText(victimProfile.getAddress());
            } else {
                addressLabel.setVisibility(View.GONE);
                address.setVisibility(View.GONE);
                address.setText(getString(R.string.volunteer_victim_address_content));
            }

            if (victimProfile != null && victimProfile.getPhone() != null && !victimProfile.getPhone().isEmpty()) {
                phoneLabel.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                phone.setText(victimProfile.getPhone());
            } else {
                phoneLabel.setVisibility(View.GONE);
                phone.setVisibility(View.GONE);
                phone.setText(getString(R.string.volunteer_victim_phone_content));
            }

            if (victimProfile != null && victimProfile.getDateOfBirth() != null && !victimProfile.getDateOfBirth().isEmpty()) {
                dateOfBirthLabel.setVisibility(View.VISIBLE);
                dateOfBirth.setVisibility(View.VISIBLE);
                dateOfBirth.setText(victimProfile.getDateOfBirth());
            } else {
                dateOfBirthLabel.setVisibility(View.GONE);
                dateOfBirth.setVisibility(View.GONE);
                dateOfBirth.setText(getString(R.string.volunteer_victim_dateofbirth_content));
            }

            if (victimProfile != null && victimProfile.getBloodType() != null && !victimProfile.getBloodType().isEmpty()) {
                bloodTypeLabel.setVisibility(View.VISIBLE);
                bloodType.setVisibility(View.VISIBLE);
                bloodType.setText(victimProfile.getBloodType());
            } else {
                bloodTypeLabel.setVisibility(View.GONE);
                bloodType.setVisibility(View.GONE);
                bloodType.setText(getString(R.string.volunteer_victim_bloodtype_content));
            }

            if (victimProfile != null && victimProfile.getAllergens() != null && !victimProfile.getAllergens().isEmpty()) {
                allergensLabel.setVisibility(View.VISIBLE);
                allergens.setVisibility(View.VISIBLE);
                allergens.setText(victimProfile.getAllergens());
            } else {
                allergensLabel.setVisibility(View.GONE);
                allergens.setVisibility(View.GONE);
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
                    .title("B???n ??? ????y")
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
                    .title("H??? ??? ????y")
                    .snippet(victimAddressLine);

            victimMarker = googleMap.addMarker(markerOptions);
            if (victimMarker != null) {
                victimMarker.showInfoWindow();
            }
        }
    }

    private void showStopVolunteerDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("X??c nh???n d???ng tr??? gi??p");
        alertDialogBuilder.setMessage("B???n c?? ch???c mu???n d???ng tr??? gi??p?");
        alertDialogBuilder.setPositiveButton("X??c nh???n", (dialog, which) -> {
            emergencyService.stopVolunteer();
            reload();
            notifyWithSnackbar("C???m ??n b???n ???? tham gia");
        });
        alertDialogBuilder.setNegativeButton("Ti???p t???c tr??? gi??p", (dialog, which) -> {
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
