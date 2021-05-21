    package hcmut.team15.emergencysupport;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static MainApplication instance;
    private static boolean isEmergencyActivityVisible;
    private static boolean isEmergencyActivityResumed;
    private final String BACKEND_URI = "http://10.0.2.2:3000/";
    private Retrofit retrofit;
    private MqttService mqttService;

    // For testing
    public static final boolean isVictim = true;
    public static final String VICTIM_USERNAME = "victim";
    public static final String VICTIM_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidmljdGltIiwiaWF0IjoxNjIxNTg4MDk1LCJleHAiOjE2NTMxMjQwOTV9.cVbIAbUEjaxCF_dRTEJCLKQBt4PXk8UNGZEvISVnW3Q";
    public static final String VOLUNTEER_USERNAME = "volunteer";
    public static final String VOLUNTEER_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_URI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        createButtonTriggerService();
        createLocationService();
    }

    private void createButtonTriggerService() {
        mqttService = new MqttService(this, "cyberproton/feeds/button");
        mqttService.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w("mqtt-connection", "Connect to server with uri " + serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.w("mqtt-connection", "Disconnected from server");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String m = message.toString();
                if (m.equals("1")) {
                    Toast.makeText(
                            getBaseContext(),
                            "Message from Adafruit: topic=" + topic + ", message=" + message.toString() + ", Button pressed",
                            Toast.LENGTH_LONG
                    ).show();

                    if (!isEmergencyActivityVisible && !isEmergencyActivityResumed) {
                        Intent intent = new Intent(getBaseContext(), EmergencyActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    private void createLocationService() {

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d("MainApplication", "Emergency Activity Started: " + activity.getLocalClassName());
        if (activity instanceof EmergencyActivity) {
            Log.d("MainApplication", "Emergency Activity Started");
            isEmergencyActivityVisible = true;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity instanceof EmergencyActivity) {
            Log.d("MainApplication", "Emergency Activity Resumed");
            isEmergencyActivityResumed = true;
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity instanceof EmergencyActivity) {
            isEmergencyActivityResumed = false;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (activity instanceof EmergencyActivity) {
            isEmergencyActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public static MainApplication getInstance() {
        return instance;
    }

    public static MqttService getMqttService() {
        return instance.mqttService;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
