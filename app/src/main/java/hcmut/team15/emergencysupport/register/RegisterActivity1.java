package hcmut.team15.emergencysupport.register;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.login.LoginActivity;

public class RegisterActivity1 extends AppCompatActivity {
    ImageView t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
    t1 = (ImageView) findViewById(R.id.iv_back);
    t1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(RegisterActivity1.this, LoginActivity.class);
            startActivity(myIntent);
        }
    });
    }
}