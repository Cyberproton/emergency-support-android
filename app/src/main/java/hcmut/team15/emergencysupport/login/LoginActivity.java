package hcmut.team15.emergencysupport.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.register.RegisterActivity1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private LoginInterface loginInterface;
    TextView t1;
    TextView t2;
    public boolean logged_state = false;
    private Call<LoginAnonymouslyResponse> loginCall;
    private AlertDialog loginConfirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        if (AccountManagement.getUserLoggedInStatus() && !AccountManagement.getLoggedInEmailUser().equals("")){
            Intent myIntent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(myIntent);
            finish();
        }
        //user-input here

        loginInterface = MainApplication.getInstance().getRetrofit().create(LoginInterface.class);
        EditText usernameText = findViewById(R.id.editTextUserName);
        EditText passwordText = findViewById(R.id.editTextTextPassword);
        //sign-in button
        t1 = findViewById(R.id.sign_up_text);
        t1.setOnClickListener(view -> {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity1.class);
                startActivity(myIntent);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng Nhập Ẩn Danh");
        builder.setMessage("Mọi dữ liệu của tài khoản ẩn danh sẽ mất khi đăng xuất. Bạn có muốn tạo tài khoản ẩn danh");
        builder.setPositiveButton("Đồng Ý", (dialog, which) -> {
            loginAnonymously();
        });
        builder.setNegativeButton("Bỏ Qua", (dialog, which) -> {
            dialog.cancel();
        });
        loginConfirmDialog = builder.create();

        //login anonymously button
        t2 = findViewById(R.id.login_anonymously_text);
        t2.setOnClickListener(view -> {
            loginConfirmDialog.show();
        });

        //sign-in button
        Button t2 = findViewById(R.id.sign_in_btn);
        t2.setOnClickListener(view -> {
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
        });
    }

    @Override
    protected void onStop() {
        if (this.loginCall != null) {
            loginCall.cancel();
            loginCall = null;
        }
        super.onStop();
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
                        AccountManagement.setUserLoggedInStatus(true);
                        TokenVar.AccessToken = response.body().getAccessToken();
                        AccountManagement.setPrefLoggedInUserEmail(TokenVar.AccessToken);
                        TokenVar.RefreshToken = response.body().getRefreshToken();
                        AccountManagement.setUsername(username);
                        if (MainApplication.getInstance().getEmergencyService() != null) {
                            MainApplication.getInstance().getEmergencyService().createSocket();
                        }
                        Intent myIntent1 = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(myIntent1);
                        finish();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Toast.makeText(getApplicationContext(),"Username does not exist or password is incorrect", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginAnonymously() {
        loginCall = loginInterface.executeLoginAnonymously();
        loginCall.enqueue(new Callback<LoginAnonymouslyResponse>() {
            @Override
            public void onResponse(@NotNull Call<LoginAnonymouslyResponse> call, @NotNull Response<LoginAnonymouslyResponse> response) {
                if (response.isSuccessful()) {
                    int code = response.code();
                    if (code == 200) {
                        AccountManagement.setUserLoggedInStatus(true);
                        TokenVar.AccessToken = response.body().getAccessToken();
                        AccountManagement.setPrefLoggedInUserEmail(TokenVar.AccessToken);
                        TokenVar.RefreshToken = response.body().getRefreshToken();
                        AccountManagement.setUsername(response.body().getUsername());
                        if (MainApplication.getInstance().getEmergencyService() != null) {
                            MainApplication.getInstance().getEmergencyService().createSocket();
                        }
                        Intent myIntent1 = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(myIntent1);
                        finish();
                    } else {

                    }
                }
                LoginActivity.this.loginCall = null;
            }

            @Override
            public void onFailure(@NotNull Call<LoginAnonymouslyResponse> call, @NotNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                LoginActivity.this.loginCall = null;
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