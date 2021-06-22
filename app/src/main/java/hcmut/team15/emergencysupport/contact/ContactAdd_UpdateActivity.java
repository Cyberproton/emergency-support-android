package hcmut.team15.emergencysupport.contact;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import hcmut.team15.emergencysupport.R;

public class ContactAdd_UpdateActivity extends AppCompatActivity {
    private EditText nameInput, phoneInput;
    private Button conrfirmBtn;
    private TextView nameWarning, phoneWarning;
    String name, phone;
    private static String nameStrWarning = "*Tên phải từ 6 kí tự trở lên chỉ bao gồm chữ và số";
    private static String phoneStrWarning = "*Nhập số điện thoại hợp lệ";
    private static String NAME_PATTERN = "^[a-zA-Z0-9]{2,}$", PHONE_PATTERN = "^[0-9]{10}$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add_update);

        nameWarning = findViewById(R.id.input_name_warning);
        phoneWarning = findViewById(R.id.input_phone_warning);
        nameInput = findViewById(R.id.contact_input_name);
        phoneInput = findViewById(R.id.contact_input_phone);
        conrfirmBtn = findViewById(R.id.contact_input_confirm);

        nameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                if(!nameValidation(nameInput.getText().toString())) nameWarning.setText(nameStrWarning);
                else nameWarning.setText("OK");
            }
        });

        phoneInput.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                if(!phoneValidation(phoneInput.getText().toString())) phoneWarning.setText(phoneStrWarning);
                else phoneWarning.setText("OK");
            }
        });

        conrfirmBtn.setOnClickListener(v -> {
            if(nameValidation(nameInput.getText().toString()) && phoneValidation(phoneInput.getText().toString())){
                Intent intent = new Intent();
                String name = nameInput.getText().toString(), phone = phoneInput.getText().toString();
                intent.putExtra("name", name);
                intent.putExtra("phone", phone);
                setResult(RESULT_OK, intent);
                finish();
            }

        });

    }

    private boolean phoneValidation(String phone) {
        return Pattern.matches(PHONE_PATTERN, phone);
    }

    private boolean nameValidation(String name) {
        return Pattern.matches(NAME_PATTERN, name);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ContactAdd_UpdateActivity.this, ContactActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}