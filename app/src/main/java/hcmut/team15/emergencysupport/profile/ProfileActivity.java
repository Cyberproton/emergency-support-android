package hcmut.team15.emergencysupport.profile;

import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.login.TokenVar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ProfileInterface profileInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        profileInterface = MainApplication.getInstance().getRetrofit().create(ProfileInterface.class);
        Button t1 = (Button) findViewById(R.id.apply_btn);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> body = new HashMap<>();
                body.put("phone","0901920444");
                profileInterface.setProfile(MainApplication.VICTIM_ACCESS, body).enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        Log.d("phone", response.body().getProfile().phone + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        Log.d("error", "error");
                    }
                });
            }
        });
    }
    protected void onStart() {
        super.onStart();
        profileInterface.getProfile(MainApplication.VICTIM_ACCESS).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                Log.d("name", response.body().getProfile().name + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.d("error", "error");
            }
        });

    }
}
