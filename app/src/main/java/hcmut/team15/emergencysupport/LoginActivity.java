package hcmut.team15.emergencysupport;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
public class LoginActivity extends AppCompatActivity {
    TextView txtNoiDung;
    Button btn_click;
    Button arrow_back;
    ImageView click;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        btn_click = (Button) findViewById(R.id.sign_in_btn);
        btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.register_activity);
            }
        });
        click = (ImageView) findViewById(R.id.iv_back);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.login_activity);
            }
        });

    }

}