package hcmut.team15.emergencysupport.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;

import hcmut.team15.emergencysupport.MainActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.register.RegisterActivity1;

public class LoginActivity extends AppCompatActivity {
    TextView t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        t1 = (TextView) findViewById(R.id.sign_up_text);

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity1.class);
                startActivity(myIntent);
            }
        });
        Button t2 = (Button) findViewById(R.id.sign_in_btn);
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent1 = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(myIntent1);
            }
        });
    }
}