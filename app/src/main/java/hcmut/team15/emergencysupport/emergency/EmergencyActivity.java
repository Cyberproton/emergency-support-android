package hcmut.team15.emergencysupport.emergency;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyActivity extends AppCompatActivity {
    private HelpRequestInterface helpRequestInterface;
    private String currentCase = "";
    private Button btn_menu, btn_emergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        helpRequestInterface = MainApplication.getInstance().getRetrofit().create(HelpRequestInterface.class);
        btn_menu = findViewById(R.id.btn_menu);
        btn_emergency = findViewById(R.id.btn_emergency);
        btn_menu.setOnClickListener(v -> {
            Intent menu_intent = new Intent(EmergencyActivity.this, MenuActivity.class);
            startActivity(menu_intent);
        });

        bindService(new Intent(this, EmergencyService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    public void sendSOS(View view) {
        Intent menu_intent = new Intent(EmergencyActivity.this, CoundownActivity.class);
        startActivity(menu_intent);
    }

    /*

    @Override
    protected void onStart() {
        super.onStart();

        if (getIntent().getBooleanExtra("startSignal", false)) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new Emergency_fragment1();

            Map<String, String> body = new HashMap<>();
            Call<Case> call = helpRequestInterface.requestHelp(body, MainApplication.VICTIM_ACCESS);
            call.enqueue(new Callback<Case>() {
                @Override
                public void onResponse(Call<Case> call, Response<Case> response) {
                    if (response.isSuccessful()) {
                        Log.d("emergency", "Help Request created with id=" + response.body().getId());
                        currentCase = response.body().getId();
                    } else {
                        Log.d("emergency", "Help Request Failed");
                    }
                }

                @Override
                public void onFailure(Call<Case> call, Throwable t) {
                    Log.d("emergency", "Help Request Failed");
                }
            });

            fragmentTransaction.replace(R.id.framecontent, fragment);
            fragmentTransaction.commit();
        }
    }

    public void replaceFragment(View v){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (v.getId()){
            case R.id.signal_button: fragment = new Emergency_fragment1();
                Map<String, String> body = new HashMap<>();
                Call<Case> call = helpRequestInterface.requestHelp(body, MainApplication.VICTIM_ACCESS);
                call.enqueue(new Callback<Case>() {
                    @Override
                    public void onResponse(Call<Case> call, Response<Case> response) {
                        if (response.isSuccessful()) {
                            Log.d("emergency", "Help Request created with id=" + response.body().getId());
                            currentCase = response.body().getId();
                        } else {
                            Log.d("emergency", "Help Request Failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<Case> call, Throwable t) {
                        Log.d("emergency", "Help Request Failed");
                    }
                });
                break;
            case R.id.stop_button: fragment = new Emergency_fragment2();
                Map<String, String> body2 = new HashMap<>();
                body2.put("case", currentCase);
                Call<Void> call2 = helpRequestInterface.stopHelp(body2, MainApplication.VICTIM_ACCESS);
                call2.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("emergency", "Help Request Closed");
                            MainApplication.getInstance().getMqttService().sendData("cyberproton/feeds/led", "0");
                        } else {
                            Log.d("emergency", "Failed to close help request");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.d("emergency", "Failed to close help request");
                    }
                });
                break;
        }

       fragmentTransaction.replace(R.id.framecontent, fragment);
       fragmentTransaction.commit();
    }
    */
}



