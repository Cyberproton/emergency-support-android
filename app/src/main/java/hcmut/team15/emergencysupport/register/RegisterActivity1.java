package hcmut.team15.emergencysupport.register;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainActivity;
import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.login.LoginActivity;
import hcmut.team15.emergencysupport.login.LoginInterface;
import hcmut.team15.emergencysupport.login.LoginResponse;
import hcmut.team15.emergencysupport.model.RegisterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity1 extends AppCompatActivity {
    ImageView t1;
    Button create_button;
    private RegisterInterface registerInterface;
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
        create_button = (Button) findViewById(R.id.create_btn);
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerInterface = MainApplication.getInstance().getRetrofit().create(RegisterInterface.class);
                if (Validation()){
                    EditText usernameText = (EditText) findViewById(R.id.editTextUserName);
                    String username = usernameText.getText().toString();
                    EditText passwordText = (EditText) findViewById(R.id.editTextTextPassword);
                    String password = passwordText.getText().toString();
                    EditText phoneText = (EditText) findViewById(R.id.editTextPhoneNumber);
                    String phone = phoneText.getText().toString();
                    Map<String, String> body = new HashMap<>();
                    body.put("username", username);
                    body.put("password", password);
                    body.put("phone", phone);

                    Call<RegisterResponse> call = registerInterface.register(body);
                    call.enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            if (response.isSuccessful()){
                                if (response.code() == 201){
                                    Intent myIntent = new Intent(RegisterActivity1.this, LoginActivity.class);
                                    startActivity(myIntent);
                                }
                                else if(response.code() == 500){
                                    Log.d("error","Could not create your account");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            Log.d("error","Could not create your account");
                        }
                    });
                }
                else {
                    Log.d("create account", "failed create account");
                }
            }
        });
    }
    public void ShowHidePass(View view){
        EditText passwordText = findViewById(R.id.editTextTextPassword);
        EditText repeatPasswordText = findViewById(R.id.editTextRepeatPassword);
        if (view.getId() == R.id.show_pass_btn1){
            if(passwordText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance()) | repeatPasswordText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                repeatPasswordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                repeatPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

        }
    }
    public boolean Validation(){
        EditText username = findViewById(R.id.editTextUserName);
        EditText password = findViewById(R.id.editTextTextPassword);
        EditText repeatPassword = findViewById(R.id.editTextRepeatPassword);
        EditText phone = findViewById(R.id.editTextPhoneNumber);
        if (username.getText().toString().length() == 0){
            username.setError("Please enter your username.");
            return false;
        }
        else if (password.getText().toString().length() == 0){
            password.setError("Please enter your password.");
            return false;
        }
        else if (repeatPassword.getText().toString().length() == 0){
            repeatPassword.setError("Please enter your confirmation password.");
            return false;
        }
        else if (phone.getText().toString().length() == 0){
            phone.setError("Please enter your phone number.");
            return false;
        }
        else if (phone.getText().toString().length() != 10){
            phone.setError("Phone number must have 10 digits.");
            return false;
        }

        else if (!password.getText().toString().equals(repeatPassword.getText().toString())){
            repeatPassword.setError("Your password and confirmation password do not match.");
            return false;
        }
        return true;
    }
}