package hcmut.team15.emergencysupport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.call.CallActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyActivity;

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

    }
}