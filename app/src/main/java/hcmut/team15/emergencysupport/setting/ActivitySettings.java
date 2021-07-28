package hcmut.team15.emergencysupport.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;

public class ActivitySettings extends AppCompatActivity {
    private static final String LISTEN_TO_BUTTON_TRIGGER = "listenToButtonTrigger";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch listenToButtonSwitch = (Switch) findViewById(R.id.listenToButtonSwitch);
        listenToButtonSwitch.setChecked(isListenToButtonTrigger());
        listenToButtonSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = MainApplication.getInstance().getSharedPreferences().edit();
            editor.putBoolean(LISTEN_TO_BUTTON_TRIGGER, isChecked);
            editor.apply();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Switch listenToButtonSwitch = (Switch) findViewById(R.id.listenToButtonSwitch);
        listenToButtonSwitch.setChecked(isListenToButtonTrigger());
    }

    public static boolean isListenToButtonTrigger() {
        return MainApplication.getInstance().getSharedPreferences().getBoolean(LISTEN_TO_BUTTON_TRIGGER, false);
    }
}
