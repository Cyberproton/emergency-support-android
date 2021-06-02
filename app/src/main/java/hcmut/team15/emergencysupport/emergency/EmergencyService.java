package hcmut.team15.emergencysupport.emergency;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.User;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class EmergencyService extends Service {
    public class LocalBinder extends Binder {
        public EmergencyService getService() {
            return EmergencyService.this;
        }
    }

    private final Emitter.Listener onVolunteerAccept = args -> {
        JSONObject body = (JSONObject) args[0];
        Log.d("EmergencyService", body.toString());
    };

    private final Emitter.Listener onCaseCreated = args -> {
        JSONObject cs = (JSONObject) args[0];
        try {
            Log.d("EmergencyService", "Case id=" + cs.getString("_id"));
            Gson gson = new Gson();
            //this.cs = gson.fromJson(cs.toString(), Case.class);
            Log.d("EmergencyService", cs.toString());
            this.cs = new Case(null, null, null, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private final Emitter.Listener onVictimCall = args -> {
        //if (EmergencyService.this.asVictim) {
        //    return;
        //}
        JSONObject victim = (JSONObject) args[0];
        try {
            Log.d("EmergencyService", "Victim id=" + victim.getString("_id"));
            Gson gson = new Gson();
            this.cs = gson.fromJson(victim.toString(), Case.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, getVictimCallNotification());
    };

    private final LocalBinder serviceBinder = new LocalBinder();
    private List<User> volunteers;
    private User you;
    private User victim;
    private Case cs;

    private Socket socket;
    private boolean isEmergencyStart;
    private boolean asVictim;
    private boolean asVolunteer;

    @Override
    public void onCreate() {
        Log.d("EmergencyService", "Service created");
        super.onCreate();
        try {
            socket = IO.socket(getString(R.string.server_url_emulator));
            handleSocket();
            socket.connect();
            Log.d("EmergencyService", "Socket IO Client connected to server with url=" + getString(R.string.server_url_emulator));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(2, getForegroundNotification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("EmergencyService", "Service bound");
        return serviceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("EmergencyService", "Service rebound");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("EmergencyService", "Service unbound");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d("EmergencyService", "Service destroyed");
        socket.disconnect();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("EmergencyService", "Service start command");

        boolean stopService = intent.getBooleanExtra("stopService", false);

        Log.d("EmergencyService", "stopService=" + stopService);
        if (stopService) {
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    public void startEmergency() {
        if (cs != null && asVolunteer) {
            throw new IllegalStateException("You cannot start as victim while you are volunteer");
        }
        socket.emit("startEmergency");
        asVictim = true;
        asVolunteer = false;
    }

    public void stopEmergency() {
        if (cs == null || asVolunteer) {
            throw new IllegalStateException("Case is already closed or you are a volunteer");
        }
        socket.emit("stopEmergency");
        cs = null;
        asVictim = false;
        asVolunteer = false;
    }

    public void startVolunteer() {
        if (cs != null && asVictim) {
            throw new IllegalStateException("You cannot start as volunteer while you are victim");
        }
        socket.emit("startVolunteer");
        asVolunteer = true;
        asVictim = false;
    }

    public void stopVolunteer() {
        if (cs == null || asVictim) {
            throw new IllegalStateException("Case is already closed or you are a victim");
        }
        socket.emit("stopVolunteer");
        asVolunteer = false;
        asVictim = false;
    }

    public List<User> getVolunteers() {
        return volunteers;
    }

    public User getYou() {
        return you;
    }

    public Case getCase() {
        return cs;
    }

    public void registerView() {

    }

    private void handleSocket() {
        socket.on(Socket.EVENT_CONNECT, (Object... args) -> {
            Log.d("EmergencyService", "Socket Connected");
        });
        socket.on(Socket.EVENT_DISCONNECT, (Object... args) -> {
            Log.d("EmergencyService", "Socket Disconnected");
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, (Object... args) -> {
            Log.d("EmergencyService", "Socket connection error");
        });
        socket.io().on(Manager.EVENT_TRANSPORT, args -> {
            Transport transport = (Transport)args[0];

            transport.on(Transport.EVENT_REQUEST_HEADERS, args12 -> {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> headers = (Map<String, List<String>>) args12[0];
                // modify request headers
                headers.put("x-access-token", Collections.singletonList(MainApplication.VICTIM_ACCESS));
            });

            transport.on(Transport.EVENT_RESPONSE_HEADERS, args1 -> {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> headers = (Map<String, List<String>>) args1[0];
                // access response headers
                //String cookie = headers.get("Set-Cookie").get(0);
            });
        });
        socket.on("caseCreated", onCaseCreated);
        socket.on("volunteerAccept", onVolunteerAccept);
        socket.on("victimCall", onVictimCall);
    }

    private Notification getForegroundNotification() {
        Intent stopServiceIntent = new Intent(this, EmergencyService.class);
        stopServiceIntent.putExtra("stopService", true);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 3, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent startEmergencyActivityIntent = PendingIntent.getActivity(
                this,
                4,
                new Intent(this, EmergencyActivity.class),
                0);

        return new NotificationCompat.Builder(this, "emergency-support-emergency")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Dịch vụ khẩn cấp đang chạy")
                .setContentText("Dịch vụ đang chạy ngầm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(R.mipmap.ic_launcher, "Đi đến phát tín hiệu", startEmergencyActivityIntent)
                //.addAction(R.mipmap.ic_launcher_round, "Tắt dịch vụ", stopServicePendingIntent)
                .build();
    }

    private Notification getVictimCallNotification() {
        return new NotificationCompat.Builder(this, "emergency-support-victim-call")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Có người cần trợ giúp gần bạn")
                .setContentText("Vui lòng hỗ trợ nếu bạn có thể")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }
}
