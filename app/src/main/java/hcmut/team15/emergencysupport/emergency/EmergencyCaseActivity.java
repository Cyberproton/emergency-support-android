package hcmut.team15.emergencysupport.emergency;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;

public class EmergencyCaseActivity extends AppCompatActivity {
    private EmergencyService emergencyService;
    private ServiceConnection emergencyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emergencyService = ((EmergencyService.LocalBinder) service).getService();
            emergencyService.startEmergency();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (emergencyService.isEmergencyStart()) {
                emergencyService.stopEmergency();
            }
            emergencyService = null;
        }
    };
    private TextView tw;
    private Button startVolunteerBtn;
    private Button stopVolunteerBtn;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_case);
        tw = findViewById(R.id.victimName);
        startVolunteerBtn = findViewById(R.id.acceptVolunteerBtn);
        stopVolunteerBtn = findViewById(R.id.stop_volunteer_btn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);

        String caseId = getIntent().getStringExtra("caseId");
        if (caseId != null) {
            Case cs = MainApplication.getInstance().getEmergencyService().getCase(caseId);
            if (cs != null) {
                tw.setText(cs.getCaller().getUsername());
                startVolunteerBtn.setOnClickListener(view -> {
                    emergencyService.acceptVolunteer(cs.getId());
                });
                stopVolunteerBtn.setOnClickListener(view -> emergencyService.stopVolunteer());
            } else {
                tw.setText("Case have been closed");
                startVolunteerBtn.setText("Case closed");
                startVolunteerBtn.setClickable(false);
                stopVolunteerBtn.setClickable(false);
            }
        }
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCaseClosed() {
        CharSequence message = "Cuộc trợ giúp đã kết thúc, cảm ơn bạn đã tham gia";
        Snackbar sb = Snackbar.make(findViewById(R.id.test_activity_case_layout), message, Snackbar.LENGTH_LONG);
        sb.show();
   }
}
