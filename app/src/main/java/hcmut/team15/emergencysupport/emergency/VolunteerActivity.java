package hcmut.team15.emergencysupport.emergency;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;

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
    private TextView dateOfBirth;
    private TextView bloodType;
    private TextView anamnesis;
    private TextView allergens;
    private Button acceptVolunteerButton;
    private Button stopVolunteerButton;

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
        dateOfBirth = findViewById(R.id.volunteer_victim_dateofbirth_content);
        bloodType = findViewById(R.id.volunteer_victim_bloodtype_content);
        anamnesis = findViewById(R.id.volunteer_victim_anamnesis_content);
        allergens = findViewById(R.id.volunteer_victim_allergens_content);
        acceptVolunteerButton = findViewById(R.id.volunteer_accept_btn);
        stopVolunteerButton = findViewById(R.id.volunteer_stop_btn);


    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCaseUpdate(Case cs) {

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
            if (callingCase.getId().equals(caseId)) {
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
            }
            if (cs != null) {
                stopVolunteerButton.setOnClickListener(view -> notifyWithSnackbar("Không thể kết thúc cuộc trợ giúp bạn chưa tham gia"));
                acceptVolunteerButton.setOnClickListener(view -> {
                    emergencyService.acceptVolunteer(cs.getId());
                    notifyWithSnackbar("Cảm ơn bạn đã tham gia");
                    acceptVolunteerButton.setText("Bạn đang tham gia cuộc trợ giúp này");
                    acceptVolunteerButton.setOnClickListener(v -> notifyWithSnackbar("Bạn đang tham gia cuộc trợ giúp này"));
                    stopVolunteerButton.setOnClickListener(v -> {
                        notifyWithSnackbar("Cảm ơn bạn đã tham gia");
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
