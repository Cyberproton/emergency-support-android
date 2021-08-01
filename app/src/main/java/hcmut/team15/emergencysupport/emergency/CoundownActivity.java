package hcmut.team15.emergencysupport.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;

public class CoundownActivity extends AppCompatActivity {
    TextView timer;
    Button btn_cancel;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coundown);

        btn_cancel = findViewById(R.id.btn_cancel);
        timer = findViewById(R.id.tv_timer);

        startTimer();
    }

    private void startTimer() {
        MainApplication.getInstance().registerCountdownActivity(this);
        countDownTimer = new CountDownTimer(5000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(""+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                Intent menu_intent = new Intent(CoundownActivity.this, NotifyFromVolunteerActivity.class);
                startActivity(menu_intent);
                MainApplication.getInstance().unregisterCountdownActivity();
                finish();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        countDownTimer.cancel();
        super.onBackPressed();
    }

    public void turnback(View view) {
        countDownTimer.cancel();
        Intent menu_intent = new Intent(CoundownActivity.this, EmergencyActivity.class);
        startActivity(menu_intent);
        MainApplication.getInstance().unregisterCountdownActivity();
        finish();
    }

    public void cancel() {
        countDownTimer.cancel();
        finish();
    }
}