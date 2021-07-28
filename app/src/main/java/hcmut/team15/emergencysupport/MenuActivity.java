package hcmut.team15.emergencysupport;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.session.MediaSession;
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
        alertDialogBuilder.setTitle("Xác nhận dừng trợ giúp");
        alertDialogBuilder.setMessage("Bạn có chắc muốn dừng trợ giúp?");
        alertDialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopVolunteer();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("Tiếp tục trợ giúp", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showStopVictimDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Xác nhận dừng phát tín hiệu");
        alertDialogBuilder.setMessage("Bạn có chắc muốn dừng phát tín hiệu?");
        alertDialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopEmergency();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("Tiếp tục phát tín hiệu", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Xác nhận đăng xuất");
        String message = "";
        message += "Nếu đăng xuất, bạn sẽ ngừng phát tín hiệu hoặc ngừng trợ giúp người khác (nếu có)\n\n";
        message += "Bạn có chắc muốn đăng xuất?\n";
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Xác nhận", (dialog, which) -> {
            EmergencyService emergencyService = MainApplication.getInstance().getEmergencyService();
            if (emergencyService != null) {
                emergencyService.stopEmergency();
            }
            logout();
        });
        alertDialogBuilder.setNegativeButton("Bỏ Qua", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}