package hcmut.team15.emergencysupport.emergency;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.HelpResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyActivity extends AppCompatActivity {
    private HelpRequestInterface helpRequestInterface;
    private String currentCase = "";
    private ServiceConnection emergencyServiceConnection;
    private EmergencyService emergencyService;
    private boolean isEmergencyServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        helpRequestInterface = MainApplication.getInstance().getRetrofit().create(HelpRequestInterface.class);
        emergencyServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                EmergencyService.LocalBinder binder = (EmergencyService.LocalBinder) service;
                emergencyService = binder.getService();
                isEmergencyServiceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                emergencyService = null;
                isEmergencyServiceBound = false;
            }
        };

        handleEmergencyService();

        Button signalButton = findViewById(R.id.signal_button);
        signalButton.setOnClickListener(view -> {
            replaceFragment(view);
            emergencyService.startEmergency();
            /*
            Map<String, String> body = new HashMap<>();
            Call<HelpResponse> call = helpRequestInterface.requestHelp(body, MainApplication.VICTIM_ACCESS);
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, Response<HelpResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("emergency", "Help Request created with id=" + response.body().getCase().getId());
                        currentCase = response.body().getCase().getId();
                    } else {
                        //Log.d("emergency", "Help Request Failed");
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    Log.d("emergency", "Help Request Failed");
                }
            });

             */

        });

        Button stopSignalButton = findViewById(R.id.stop_button);
        stopSignalButton.setOnClickListener(view -> {
            replaceFragment(view);
            emergencyService.stopEmergency();
            /*
            Map<String, String> body2 = new HashMap<>();
            body2.put("case", currentCase);
            Call<Void> call2 = helpRequestInterface.stopHelp(body2, MainApplication.VICTIM_ACCESS);
            call2.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("emergency", "Help Request Closed");
                        //MainApplication.getInstance().getMqttService().sendData("cyberproton/feeds/led", "0");
                    } else {
                        Log.d("emergency", "Failed to close help request");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("emergency", "Failed to close help request");
                }
            });*/
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (getIntent().getBooleanExtra("startSignal", false)) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new Emergency_fragment1();

            Map<String, String> body = new HashMap<>();
            Call<HelpResponse> call = helpRequestInterface.requestHelp(body, MainApplication.VICTIM_ACCESS);
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, Response<HelpResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("emergency", "Help Request created with id=" + response.body().getCase().getId());
                        currentCase = response.body().getCase().getId();
                    } else {
                        Log.d("emergency", "Help Request Failed");
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    Log.d("emergency", "Help Request Failed");
                }
            });

            fragmentTransaction.replace(R.id.framecontent, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onStop() {
        if (isEmergencyServiceBound) {
            unbindService(emergencyServiceConnection);
            isEmergencyServiceBound = false;
        }
        super.onStop();
    }

    public void replaceFragment(View v){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (v.getId()){
            case R.id.signal_button:
                fragment = new Emergency_fragment1();
                break;
            case R.id.stop_button:
                fragment = new Emergency_fragment2();
                break;
        }

       fragmentTransaction.replace(R.id.framecontent, fragment);
       fragmentTransaction.commit();
    }

    private void handleEmergencyService() {
        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
    }

    private void unhandleEmergencyService() {
        if (isEmergencyServiceBound) {
            unbindService(emergencyServiceConnection);
            isEmergencyServiceBound = false;
        }
    }
}



