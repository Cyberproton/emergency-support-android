package hcmut.team15.emergencysupport.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import android.widget.Toast;

import hcmut.team15.emergencysupport.MainActivity;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.register.RegisterActivity1;
import hcmut.team15.emergencysupport.MainApplication;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private LoginInterface loginInterface;
    TextView t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        //user-input here

        loginInterface = MainApplication.getInstance().getRetrofit().create(LoginInterface.class);
        EditText usernameText = findViewById(R.id.editTextUserName);
        EditText passwordText = findViewById(R.id.editTextTextPassword);
        //sign-in button
        t1 = (TextView) findViewById(R.id.sign_up_text);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity1.class);
                startActivity(myIntent);

            }
        });
        //forgot password textview
        TextView t3 = (TextView) findViewById(R.id.f_password);
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(myIntent);
            }
        });

        //sign-in button
        Button t2 = (Button) findViewById(R.id.sign_in_btn);
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                if (username.length() == 0){
                    usernameText.setError("Please Enter your Username");
                }
                else if (password.length() == 0){
                    passwordText.setError("Please Enter your Password");
                }
                else {
                    Login(username, password);
                }
            }
        });

    }
    private void Login(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        Call<LoginResponse> call = loginInterface.executeLogin(body);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    int responseStatusCode = response.code();
                    if (responseStatusCode == 200){
                        TokenVar.AccessToken = response.body().getAccessToken();
                        TokenVar.RefreshToken = response.body().getRefreshToken();
                        Intent myIntent1 = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(myIntent1);
                    }
                }
                Toast.makeText(getApplicationContext(),"Username does not exist or password is incorrect", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Toast.makeText(getApplicationContext(),"Username does not exist or password is incorrect", Toast.LENGTH_LONG).show();
            }
        });
    }
    // Click to change between (visible|invisible) of password text
    public void ShowHidePass(View view){
        EditText passwordText = findViewById(R.id.editTextTextPassword);
        if (view.getId() == R.id.show_pass_btn2){
            if(passwordText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

        }
    }
}