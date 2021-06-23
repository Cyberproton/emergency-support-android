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
    private Button acceptVolunteerButton;
    private Button stopVolunteerButton;

    private Location latestLocation;
    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private double lat, lng;
    private String addressLine;
    private Marker volunteerMarker;
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
        acceptVolunteerButton = findViewById(R.id.volunteer_accept_btn);
        stopVolunteerButton = findViewById(R.id.volunteer_stop_btn);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.volunteer_map);
        android.location.Location loc = MainApplication.getInstance().getLocationService().getLastLocation();
        if (loc == null) {
            latestLocation = new Location(0, 0, 0);
        } else {
            latestLocation = new Location(loc);
        }
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
                try {
                    geocoder = new Geocoder(VolunteerActivity.this
                            , Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(
                            latestLocation.getLatitude(), latestLocation.getLongitude(), 1
                    );

                    lat = addresses.get(0).getLatitude();
                    lng = addresses.get(0).getLongitude();
                    addressLine = addresses.get(0).getAddressLine(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                LatLng latLng = new LatLng(lat, lng);
                String markerTitle = addressLine != null ? addressLine : "Bạn ở đây";
                MarkerOptions options = new MarkerOptions().position(latLng).title(markerTitle);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                volunteerMarker = googleMap.addMarker(options);
                if (volunteerMarker != null) {
                    volunteerMarker.showInfoWindow();
                }
                VolunteerActivity.this.googleMap = googleMap;
            }
        });
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCaseUpdate(Case cs) {
        reload();
    }

    private void reload() {
        Case acceptedCase = emergencyService.getAcceptedCase();
        Case callingCase = emergencyService.getCallingCase();
        caseId = getIntent().getStringExtra("caseId");

        if (callingCase != null) {
            acceptVolunteerButton.setText("Bạn đang phát tín hiệu khẩn cấp");
            acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang phát tín hiệu khẩn cấp rồi"));
            stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
            return;
        } else if (acceptedCase != null) {
            if (acceptedCase.getId().equals(caseId)) {
                acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp này");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang tham gia cuộc trợ giúp này"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
            } else {
                acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp khác");
                acceptVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Bạn đang tình nguyện cho một người khác rồi"));
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
            }
            return;
        } else {
            if (caseId != null) {
                cs = emergencyService.getCase(caseId);
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
            if (cs != null) {
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
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
            return;
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
