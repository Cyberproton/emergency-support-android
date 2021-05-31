package hcmut.team15.emergencysupport.location;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {
    private final Binder serviceBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationRequest fastLocationRequest;
    private LocationRequestInterface locationRequestInterface;
    private Location lastLocation;
    private boolean isRequestingLocationUpdates;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationReceived(locationResult.getLastLocation());
            }
        };
        locationRequest = LocationRequest
                .create()
                .setInterval(15000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fastLocationRequest = LocationRequest
                .create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        updateLastLocation();
        locationRequestInterface = MainApplication.getInstance().getRetrofit().create(LocationRequestInterface.class);
        Log.d("LocationService", "Location Service Created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Notification notification = new NotificationCompat.Builder(this, "emergency-support-location")
                .setOngoing(true)
                .setContentTitle("Emergency Support")
                .setContentText("Ứng dụng đang chạy ngầm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
        return serviceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Notification notification = new NotificationCompat.Builder(this, "emergency-support-location")
                .setOngoing(true)
                .setContentTitle("Emergency Support")
                .setContentText("Ứng dụng đang chạy ngầm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && isRequestingLocationUpdates) {
            //startForeground(NOTIFICATION_ID, notification);
        }
        stopForeground(true);
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch (SecurityException ex) {
            Log.e("LocationService", "Location Permission may have not been granted");
        }

        return START_STICKY;
    }

    public void requestLocationUpdates() {
        startService(new Intent(MainApplication.getInstance(), getClass()));
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            isRequestingLocationUpdates = true;
        } catch (SecurityException ex) {
            Log.e("LocationService", "Location Permission may have not been granted");
        }
    }

    public void removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isRequestingLocationUpdates = false;
            stopSelf();
        } catch (SecurityException ex) {
            Log.e("LocationService", "Location Permission may have not been granted");
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isRequestingLocationUpdates() {
        return isRequestingLocationUpdates;
    }

    private void onLocationReceived(Location location) {
        Log.d("LocationService", "Location received: " + location.toString());
        Map<String, String> body = new HashMap<>();
        body.put("longitude", String.valueOf(location.getLongitude()));
        body.put("latitude", String.valueOf(location.getLatitude()));
        body.put("altitude", String.valueOf(location.getAltitude()));
        String token;
        if (MainApplication.isVictim) {
            token = MainApplication.VICTIM_ACCESS;
        } else {
            token = MainApplication.VOLUNTEER_ACCESS;
        }
        Call<Void> call = locationRequestInterface.updateMyLocation(body, token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("LocationService", "Location updated on server");
                } else {
                    Log.d("LocationService", "Failed to update Location on server");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("LocationService", "Failed to update Location on server");
            }
        });
    }

    private void updateLastLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        lastLocation = task.getResult();
                    }
                }
            });
        } catch (SecurityException ex) {
            Log.e("LocationService", "Location Permission may have not been granted");
        }
    }
}
