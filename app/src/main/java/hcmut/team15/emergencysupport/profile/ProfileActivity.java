package hcmut.team15.emergencysupport.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.login.AccountManagement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ProfileInterface profileInterface;
    private EditText name;
    private EditText phone;
    private EditText address;
    private EditText dateOfBirth;
    private EditText bloodType;
    private EditText allergy;
    private Switch nameSwitch;
    private Switch phoneSwitch;
    private Switch addressSwitch;
    private Switch dateOfBirthSwitch;
    private Switch bloodTypeSwitch;
    private Switch allergensSwitch;
    private DatePickerDialog datePickerDialog;
    private DatePickerDialog.OnDateSetListener onDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        profileInterface = MainApplication.getInstance().getRetrofit().create(ProfileInterface.class);

        name = findViewById(R.id.editTextName);
        phone = findViewById(R.id.editTextPhoneNumber);
        address = findViewById(R.id.editTextAddress);
        dateOfBirth = findViewById(R.id.editTextBirthDay);
        bloodType = findViewById(R.id.editTextBloodType);
        allergy = findViewById(R.id.editTextAllergy);

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateOfBirth.setText(dayOfMonth + "/" + month + "/" + year);
            }
        };
        datePickerDialog = new DatePickerDialog(this, onDateSetListener, 2000, 1, 1);
        dateOfBirth.setOnClickListener(view -> {
            datePickerDialog.show();
        });
        nameSwitch = findViewById(R.id.nameVisibility);
        nameSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });
        phoneSwitch = findViewById(R.id.phoneVisibility);
        phoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });
        addressSwitch = findViewById(R.id.addressVisibility);
        addressSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });
        dateOfBirthSwitch = findViewById(R.id.dateOfBirthVisibility);
        dateOfBirthSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });
        bloodTypeSwitch = findViewById(R.id.bloodTypeVisibility);
        bloodTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });
        allergensSwitch = findViewById(R.id.allergensVisibility);
        allergensSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                buttonView.setText(R.string.profile_attribute_show);
            } else {
                buttonView.setText(R.string.profile_attribute_hide);
            }
        });

        Button apply_btn = findViewById(R.id.apply_btn);
        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> body = new HashMap<>();
                body.put("name", name.getText().toString());
                body.put("phone", phone.getText().toString());
                body.put("address", address.getText().toString());
                body.put("dateOfBirth", dateOfBirth.getText().toString());
                body.put("bloodType", bloodType.getText().toString());
                body.put("allergens", allergy.getText().toString());
                body.put("nameVisibility", String.valueOf(nameSwitch.isChecked()));
                body.put("phoneVisibility", String.valueOf(phoneSwitch.isChecked()));
                body.put("addressVisibility", String.valueOf(addressSwitch.isChecked()));
                body.put("dateOfBirthVisibility", String.valueOf(dateOfBirthSwitch.isChecked()));
                body.put("bloodTypeVisibility", String.valueOf(bloodTypeSwitch.isChecked()));
                body.put("allergensVisibility", String.valueOf(allergensSwitch.isChecked()));
                profileInterface.setProfile(AccountManagement.getUserAccessToken(), body).enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        Toast.makeText(getApplicationContext(),"Cập nhật thông tin thành công", Toast.LENGTH_LONG).show();
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
        profileInterface.getProfile(AccountManagement.getUserAccessToken()).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.code() == 200){
                    if (response.body().getProfile() != null) {
                        name.setText(response.body().getProfile().name);
                        phone.setText(response.body().getProfile().phone);
                        address.setText(response.body().getProfile().address);
                        dateOfBirth.setText(response.body().getProfile().dateOfBirth);
                        bloodType.setText(response.body().getProfile().bloodType);
                        allergy.setText(response.body().getProfile().allergens);
                        nameSwitch.setChecked(response.body().getProfile().nameVisibility);
                        if (nameSwitch.isChecked()) {
                            nameSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            nameSwitch.setText(R.string.profile_attribute_hide);
                        }
                        phoneSwitch.setChecked(response.body().getProfile().phoneVisibility);
                        if (phoneSwitch.isChecked()) {
                            phoneSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            phoneSwitch.setText(R.string.profile_attribute_hide);
                        }
                        addressSwitch.setChecked(response.body().getProfile().addressVisibility);
                        if (addressSwitch.isChecked()) {
                            addressSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            addressSwitch.setText(R.string.profile_attribute_hide);
                        }
                        dateOfBirthSwitch.setChecked(response.body().getProfile().dateOfBirthVisibility);
                        if (dateOfBirthSwitch.isChecked()) {
                            dateOfBirthSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            dateOfBirthSwitch.setText(R.string.profile_attribute_hide);
                        }
                        bloodTypeSwitch.setChecked(response.body().getProfile().bloodTypeVisibility);
                        if (bloodTypeSwitch.isChecked()) {
                            bloodTypeSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            bloodTypeSwitch.setText(R.string.profile_attribute_hide);
                        }
                        allergensSwitch.setChecked(response.body().getProfile().allergensVisibility);
                        if (allergensSwitch.isChecked()) {
                            allergensSwitch.setText(R.string.profile_attribute_show);
                        } else {
                            allergensSwitch.setText(R.string.profile_attribute_hide);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.d(ProfileActivity.class.getSimpleName(), "Error: " + t.getMessage());
            }
        });
    }
}
