package hcmut.team15.emergencysupport.profile;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmut.team15.emergencysupport.MainActivity;
import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
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
        Button apply_btn = findViewById(R.id.apply_btn);
        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameText = findViewById(R.id.editTextName);
                EditText phoneText = findViewById(R.id.editTextPhoneNumber);
                EditText addressText = findViewById(R.id.editTextAddress);
                EditText dateOfBirthText = findViewById(R.id.editTextBirthDay);
                EditText bloodTypeText = findViewById(R.id.editTextBloodType);
                EditText allergyText = findViewById(R.id.editTextAllergy);
                Map<String, String> body = new HashMap<>();
                body.put("name", nameText.getText().toString());
                body.put("phone", phoneText.getText().toString());
                body.put("address", addressText.getText().toString());
                body.put("dateOfBirth", dateOfBirthText.getText().toString());
                body.put("bloodType", bloodTypeText.getText().toString());
                body.put("allergens", allergyText.getText().toString());
                profileInterface.setProfile(TokenVar.AccessToken, body).enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        Toast.makeText(getApplicationContext(),"Approved change request", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        Log.d("error", "error");
                    }
                });
            }
        });
        ImageView back_btn = findViewById(R.id.iv_back);
        back_btn.setOnClickListener(view ->{
            Intent myIntent = new Intent(ProfileActivity.this, MenuActivity.class);
            startActivity(myIntent);
        });
    }
    protected void onStart() {
        super.onStart();
        EditText name = findViewById(R.id.editTextName);
        EditText phone = findViewById(R.id.editTextPhoneNumber);
        EditText address = findViewById(R.id.editTextAddress);
        EditText dateOfBirth = findViewById(R.id.editTextBirthDay);
        EditText bloodType = findViewById(R.id.editTextBloodType);
        EditText allergy = findViewById(R.id.editTextAllergy);

        profileInterface.getProfile(TokenVar.AccessToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                Log.d("name", response.body().getProfile().name + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                if (response.code() == 200){
                      name.setText(response.body().getProfile().name);
                      phone.setText(response.body().getProfile().phone);
                      address.setText(response.body().getProfile().address);
                      dateOfBirth.setText(response.body().getProfile().dateOfBirth);
                      bloodType.setText(response.body().getProfile().bloodType);
                      allergy.setText(response.body().getProfile().allergens);
                }
            }
            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.d("error", "error");
            }
        });

    }
}
