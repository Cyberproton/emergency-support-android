package hcmut.team15.emergencysupport.emergency;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;

public class CoundownActivity extends AppCompatActivity {
    TextView timer;
    Button btn_cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coundown);

        btn_cancel = findViewById(R.id.btn_cancel);
        timer = findViewById(R.id.tv_timer);

        startTimer();
    }

    private void startTimer() {
        new CountDownTimer(5000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(""+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "DONE", Toast.LENGTH_LONG).show();
                Intent menu_intent = new Intent(CoundownActivity.this, NotifyFromVolunteerActivity.class);
                startActivity(menu_intent);
            }
        }.start();
    }


    public void turnback(View view) {
        Intent menu_intent = new Intent(CoundownActivity.this, EmergencyActivity.class);
        startActivity(menu_intent);
    }
}