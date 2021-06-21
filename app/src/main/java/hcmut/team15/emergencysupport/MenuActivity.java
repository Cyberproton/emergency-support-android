package hcmut.team15.emergencysupport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.call.CallActivity;
import hcmut.team15.emergencysupport.contact.ContactActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyActivity;
import hcmut.team15.emergencysupport.login.ForgotPasswordActivity;
import hcmut.team15.emergencysupport.login.LoginActivity;
import hcmut.team15.emergencysupport.profile.ProfileActivity;

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

        Button contact = findViewById(R.id.menu_contacts_btn);
        contact.setOnClickListener(view ->{
            Intent myIntent = new Intent(MenuActivity.this, ContactActivity.class);
            startActivity(myIntent);
        });
    }
}