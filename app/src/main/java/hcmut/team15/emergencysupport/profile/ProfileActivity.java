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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    //Google-map
    SupportMapFragment supportMapFragment;
    double lat,lng;
    String addressLine;
    TextView userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        //Google-map
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.userMap);
        userLocation = findViewById(R.id.userLocation);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
                try {
                    Geocoder geocoder = new Geocoder(ProfileActivity.this
                            , Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(
                            60, 60, 1
                    );

                    lat = addresses.get(0).getLatitude();
                    lng = addresses.get(0).getLongitude();
                    addressLine = addresses.get(0).getAddressLine(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                userLocation.setText("Tôi đang đứng ở: " + addressLine);
                LatLng latLng = new LatLng(lat, lng);
                MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                googleMap.addMarker(options);
            }
        });

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
                String name = nameText.getText().toString();
                String phone = phoneText.getText().toString();
                String address = addressText.getText().toString();
                String dateOfBirth = dateOfBirthText.getText().toString();
                String bloodType = bloodTypeText.getText().toString();
                String allergy = allergyText.getText().toString();
                Map<String, String> body = new HashMap<>();
                body.put("name", name);
                body.put("phone", phone);
                body.put("address", address);
                body.put("dateOfBirth", dateOfBirth);
                body.put("bloodType", bloodType);
                body.put("allergens", allergy);
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
