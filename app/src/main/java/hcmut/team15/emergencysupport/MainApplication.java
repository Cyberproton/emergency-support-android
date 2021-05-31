    package hcmut.team15.emergencysupport;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hcmut.team15.emergencysupport.hardware.MqttService;
import hcmut.team15.emergencysupport.emergency.EmergencyActivity;
import hcmut.team15.emergencysupport.location.LocationService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static MainApplication instance;
    private static boolean isEmergencyActivityVisible;
    private static boolean isEmergencyActivityResumed;
    private final String BACKEND_URI = "http://10.0.2.2:3000/";
    private final String REAL_URI = "http://192.168.1.16:3000";
    private Retrofit retrofit;
    private MqttService mqttService;
    private LocationService locationService;

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
        createNotificationChannel();
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
                        intent.putExtra("startSignal", true);
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
        bindService(new Intent(this, LocationService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                locationService = ((LocationService.LocalBinder) service).getService();
                locationService.requestLocationUpdates();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService.removeLocationUpdates();
                locationService = null;
            }
        }, BIND_AUTO_CREATE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainApplication", "Creating Notification Channel");
            String channelId = "emergency-support-location";
            CharSequence name = "Emergency Support Channel";
            String description = "No Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
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

    public MqttService getMqttService() {
        return mqttService;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
