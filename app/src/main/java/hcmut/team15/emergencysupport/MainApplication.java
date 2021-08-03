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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hcmut.team15.emergencysupport.emergency.CoundownActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyActivity;
import hcmut.team15.emergencysupport.emergency.EmergencyService;
import hcmut.team15.emergencysupport.emergency.NotifyFromVolunteerActivity;
import hcmut.team15.emergencysupport.hardware.MqttService;
import hcmut.team15.emergencysupport.location.LocationService;
import hcmut.team15.emergencysupport.login.AccountManagement;
import hcmut.team15.emergencysupport.model.User;
import hcmut.team15.emergencysupport.setting.ActivitySettings;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static MainApplication instance;
    private static boolean isEmergencyActivityVisible;
    private static boolean isEmergencyActivityResumed;
    public static String BACKEND_URI = "http://10.0.2.2:3000/";
    public static final String LOCAL_URL = "http://10.0.2.2:3000/";
    public static final String REAL_URI = "http://192.168.1.16:3000";
    private Retrofit retrofit;
    private MqttService mqttService;
    private LocationService locationService;
    private EmergencyService emergencyService;

    private NotifyFromVolunteerActivity notifyFromVolunteerActivity;
    private CoundownActivity coundownActivity;

    // For testing
    public static User you;
    public static final boolean isVictim = true;
    public static final String VICTIM_USERNAME = "victim";
    public static String VICTIM_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidmljdGltIiwiaWF0IjoxNjIxNTg4MDk1LCJleHAiOjE2NTMxMjQwOTV9.cVbIAbUEjaxCF_dRTEJCLKQBt4PXk8UNGZEvISVnW3Q";
    public static final String VOLUNTEER_USERNAME = "volunteer";
    public static final String VOLUNTEER_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //you = new User(VICTIM_USERNAME, "", null);
        createNotificationChannel();
        createLocationService();
        createButtonTriggerService();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void createRetrofit(String serverUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void createButtonTriggerService() {
        String touchFeed = getString(R.string.touch_feed);
        String ledFeed = getString(R.string.led_feed);
        mqttService = new MqttService(this, touchFeed, ledFeed);
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
                Log.d("MqttService", "Receive message from topic=" + topic + " and message=" + message.toString());

                if (topic.equals(touchFeed)) {
                    JsonObject json = new JsonParser().parse(m).getAsJsonObject();
                    String id = json.get("id").getAsString();
                    String name = json.get("name").getAsString();
                    String data = json.get("data").getAsString();
                    String unit = json.get("unit").getAsString();

                    Log.d("MqttService", "id: " + json.get("id").getAsString());
                    Log.d("MqttService", "name: " + json.get("name").getAsString());
                    Log.d("MqttService", "data: " + json.get("data").getAsString());
                    Log.d("MqttService", "unit: " + json.get("unit").getAsString());

                    if (name.equals("TOUCH") && data.equals("1")) {
                        Log.d(MainApplication.class.getSimpleName(), "Perform check: ");
                        if (emergencyService != null && ActivitySettings.isListenToButtonTrigger() && AccountManagement.getUserLoggedInStatus()) {
                            Log.d(MainApplication.class.getSimpleName(), "Checking countdown");
                            if (coundownActivity != null) {
                                coundownActivity.cancel();
                            }
                            Log.d(MainApplication.class.getSimpleName(), "Checking NotificationFromVolunteer");
                            if (notifyFromVolunteerActivity == null) {
                                Log.d(MainApplication.class.getSimpleName(), "Start from button");
                                Intent intent = new Intent(MainApplication.this, NotifyFromVolunteerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("startFromButton", true);
                                startActivity(intent);
                            } else {
                                Log.d(MainApplication.class.getSimpleName(), "Stop from button");
                                notifyFromVolunteerActivity.stopEmergency();
                                Toast.makeText(MainApplication.this, "Nhận tín hiệu từ " + name + ", dừng phát tín hiệu", Toast.LENGTH_LONG).show();
                            }
                        }
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

        bindService(new Intent(this, EmergencyService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                emergencyService = ((EmergencyService.LocalBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                emergencyService = null;
            }
        }, BIND_AUTO_CREATE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainApplication", "Creating Location Notification Channel");
            String channelId = "emergency-support-location";
            CharSequence name = "Dịch vụ vị trí";
            String description = "Ứng dụng gửi vị trí của bạn lên máy chủ";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainApplication", "Creating Emergency Notification Channel");
            String channelId = "emergency-support-emergency";
            CharSequence name = "Dịch vụ khẩn cấp";
            String description = "Ứng dụng chạy chức năng phát tín hiệu khẩn cấp khi bạn tắt ứng dụng";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainApplication", "Creating Notification Channel");
            String channelId = "emergency-support-victim-call";
            CharSequence name = "Người trợ giúp gần bạn";
            String description = "Thông báo khi có người cần trợ giúp gần bạn";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainApplication", "Creating Volunteer Accept Notification Channel");
            String channelId = "emergency-support-volunteer-accept";
            CharSequence name = "Tình nguyện viên giúp đỡ";
            String description = "Thông báo khi có tình nguyện chấp nhận trợ giúp";
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

    public void onLocationPermissionRequested() {
        Log.d("MainApplication", "locationService: " +( locationService == null));
        Log.d("MainApplication", "locationService: " +( locationService.isRequestingLocationUpdates()));
        if (locationService != null && !locationService.isRequestingLocationUpdates()) {
            locationService.requestLocationUpdates();
        }
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

    public LocationService getLocationService() {
        return locationService;
    }

    public EmergencyService getEmergencyService() {
        return emergencyService;
    }

    public SharedPreferences getSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void registerNotifyFromVolunteerActivity(NotifyFromVolunteerActivity activity) {
        notifyFromVolunteerActivity = activity;
    }

    public void unregisterNotifyFromVolunteerActivity() {
        notifyFromVolunteerActivity = null;
    }

    public boolean isNotifyFromVolunteerActivityRegistered() {
        return notifyFromVolunteerActivity != null;
    }

    public void registerCountdownActivity(CoundownActivity coundownActivity) {
        this.coundownActivity = coundownActivity;
    }

    public void unregisterCountdownActivity() {
        this.coundownActivity = null;
    }
}
