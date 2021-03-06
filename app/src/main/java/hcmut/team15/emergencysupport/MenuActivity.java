package hcmut.team15.emergencysupport;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import hcmut.team15.emergencysupport.call.CallActivity;
import hcmut.team15.emergencysupport.contact.ContactActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyService;
import hcmut.team15.emergencysupport.login.AccountManagement;
import hcmut.team15.emergencysupport.login.LoginActivity;
import hcmut.team15.emergencysupport.login.TokenVar;
import hcmut.team15.emergencysupport.profile.ProfileActivity;
import hcmut.team15.emergencysupport.setting.ActivitySettings;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button emergencyStartBtn = findViewById(R.id.menu_emergency_start_btn);
        emergencyStartBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, EmergencyActivity.class);
            startActivity(intent);
        });
        Button victimCallsBtn = findViewById(R.id.menu_victim_calls_btn);
        victimCallsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, CallActivity.class);
            startActivity(intent);
        });
        Button profile = findViewById(R.id.menu_user_info_btn);
        profile.setOnClickListener(view ->{
            Intent myIntent = new Intent(MenuActivity.this, ProfileActivity.class);
            startActivity(myIntent);
        });
        Button logout = findViewById(R.id.menu_logout_btn);
        logout.setOnClickListener(view -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            showLogoutDialog();
        });

        Button contact = findViewById(R.id.menu_contacts_btn);
        contact.setOnClickListener(view ->{
            Intent myIntent = new Intent(MenuActivity.this, ContactActivity.class);
            startActivity(myIntent);
        });

        Button settings = findViewById(R.id.menu_settings_btn);
        settings.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, ActivitySettings.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                MainApplication.getInstance().onLocationPermissionRequested();
                Log.d("MainActivity", "Bind Location Service");
            } else {
                Log.d("MainActivity", "Request permission for Location Service");
                requestPermissions(new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                MainApplication.getInstance().onLocationPermissionRequested();
                Log.d("MainActivity", "Bind Location Service");
            } else {
                Log.d("MainActivity", "Request permission for Location Service");
                ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            }
        }
    }

    private void logout() {
        EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
        if (emergencyService != null) {
            emergencyService.disconnectSocket();
        }
        AccountManagement.clearLoggedInState();
        TokenVar.AccessToken = "";
        Intent myIntent = new Intent(MenuActivity.this, LoginActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myIntent);
    }

    private void showStopVolunteerDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("X??c nh???n d???ng tr??? gi??p");
        alertDialogBuilder.setMessage("B???n c?? ch???c mu???n d???ng tr??? gi??p?");
        alertDialogBuilder.setPositiveButton("X??c nh???n", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopVolunteer();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("Ti???p t???c tr??? gi??p", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showStopVictimDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("X??c nh???n d???ng ph??t t??n hi???u");
        alertDialogBuilder.setMessage("B???n c?? ch???c mu???n d???ng ph??t t??n hi???u?");
        alertDialogBuilder.setPositiveButton("X??c nh???n", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopEmergency();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("Ti???p t???c ph??t t??n hi???u", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("X??c nh???n ????ng xu???t");
        String message = "";
        message += "N???u ????ng xu???t, b???n s??? ng???ng ph??t t??n hi???u ho???c ng???ng tr??? gi??p ng?????i kh??c (n???u c??)\n\n";
        message += "N???u b???n ????ng nh???p ???n danh, m???i d??? li???u s??? b??? m???t\n\n";
        message += "B???n c?? ch???c mu???n ????ng xu???t?\n";
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("X??c nh???n", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopEmergency();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("B??? Qua", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}