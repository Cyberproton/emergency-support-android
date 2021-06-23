package hcmut.team15.emergencysupport.emergency;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.call.CallActivity;
import hcmut.team15.emergencysupport.login.AccountManagement;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
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
        Log.d(this.getClass().getSimpleName(), "Volunteer accepted");
        try {
            if (!this.asVictim || this.callingCase == null) {
                return;
            }

            JSONObject body = null;
            User volunteer = null;
            body = (JSONObject) args[0];
            JSONObject jsonCase = (JSONObject) args[1];
            Gson gson = new Gson();
            volunteer = gson.fromJson(body.toString(), User.class);
            Case cs = gson.fromJson(jsonCase.toString(), Case.class);
            handleCaseUpdate(cs);

            float dist = Location.distanceBetween(cs.getCaller().getCurrentLocation(), volunteer.getCurrentLocation());
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(volunteerAcceptNotificationId, getVolunteerAcceptNotification(volunteer.getUsername(), dist));
            volunteerAcceptNotificationId++;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onVolunteerStop = args -> {
        Log.d(this.getClass().getSimpleName(), "Volunteer stop");
        if (this.asVictim && this.callingCase != null) {
            try {
                JSONObject jsonCase = (JSONObject) args[1];
                Case cs = new Gson().fromJson(jsonCase.toString(), Case.class);
                handleCaseUpdate(cs);
                if (this.notifyFromVolunteerActivity != null) {
                    this.notifyFromVolunteerActivity.onCaseClosed();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private final Emitter.Listener onVolunteerUpdate = args -> {
        Log.d(this.getClass().getSimpleName(), "Volunteer Update");
        try {
            JSONObject jsonCase = (JSONObject) args[0];
            Gson gson = new Gson();
            Case cs = gson.fromJson(jsonCase.toString(), Case.class);
            handleCaseUpdate(cs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onCaseCreated = args -> {
        Log.d(getClass().getSimpleName(), "Case Created");
        try {
            JSONObject cs = (JSONObject) args[0];
            Gson gson = new Gson();
            this.callingCase = gson.fromJson(cs.toString(), Case.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onCaseClosed = args -> {
        Log.d(getClass().getSimpleName(), "Case Closed");
        try {
            JSONObject jsonCase = (JSONObject) args[0];
            Gson gson = new Gson();
            Case cs = gson.fromJson(jsonCase.toString(), Case.class);
            if (this.asVolunteer && this.acceptedCase != null && this.acceptedCase.getId().equals(cs.getId())) {
                this.acceptedCase = null;
            }
            this.cases.remove(cs.getId());
            handleCaseUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onVictimCall = args -> {
        try {
            Log.d(this.getClass().getSimpleName(), "Victim call");

            JSONObject victim = (JSONObject) args[0];
            JSONObject jsonCase = (JSONObject) args[1];
            JSONObject jsonYou = (JSONObject) args[2];

            User you = new Gson().fromJson(jsonYou.toString(), User.class);
            Case cs = new Gson().fromJson(jsonCase.toString(), Case.class);

            boolean shouldNotify = !this.cases.containsKey(cs.getId());
            handleCaseUpdate(cs);

            Log.d(getClass().getSimpleName(), "Should notify: " + shouldNotify);

            if (this.asVictim || this.asVolunteer) {
                return;
            }

            Log.d(getClass().getSimpleName(), "Check notify: " + shouldNotify);

            if (shouldNotify) {
                float dist = Location.distanceBetween(cs.getCaller().getCurrentLocation(), you.getCurrentLocation());
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(victimCallNotificationId, getVictimCallNotification(dist, cs));
                victimCallNotificationId++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final LocalBinder serviceBinder = new LocalBinder();
    private final Map<String, Case> cases = new HashMap<>();

    private Socket socket;
    private boolean isEmergencyStart;

    private Case acceptedCase;
    private boolean asVolunteer = false;

    private Case callingCase;
    private boolean asVictim = false;

    private static int victimCallNotificationId = 1000;
    private static int volunteerAcceptNotificationId = 2000;
    private NotifyFromVolunteerActivity notifyFromVolunteerActivity;
    private EmergencyCaseActivity emergencyCaseActivity;
    private CallActivity callActivity;
    private VolunteerActivity volunteerActivity;

    @Override
    public void onCreate() {
        Log.d("EmergencyService", "Service created");
        super.onCreate();
        createSocket(getString(R.string.server_url));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(2, getForegroundNotification());
        }
    }

    public void createSocket(String serverUrl) {
        if (AccountManagement.getUserAccessToken() == null) {
            Log.w("EmergencyService", "Unable to create socket because user token is null");
            return;
        }
        try {
            if (socket != null) {
                return;
            }
            socket = IO.socket(serverUrl);
            handleSocket();
            socket.connect();
            Log.d("EmergencyService", "Socket IO Client connected to server with url=" + serverUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void createSocket() {
        createSocket(getString(R.string.server_url));
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

    public void handleCaseUpdate(Case cs) {
        if (asVictim && callingCase != null && callingCase.getId().equals(cs.getId())) {
            this.callingCase = cs;
        } else if (asVolunteer && acceptedCase != null && acceptedCase.getId().equals(cs.getId())) {
            this.acceptedCase = cs;
        }
        cases.put(cs.getId(), cs);
        for (Map.Entry<String, Case> stringCaseEntry : cases.entrySet()) {
            Log.d("EmergencyService Case", stringCaseEntry.getKey() + "  " + stringCaseEntry.getValue().getId());
        }
        if (notifyFromVolunteerActivity != null) {
            notifyFromVolunteerActivity.runOnUiThread(() -> notifyFromVolunteerActivity.updateCase(cs));
        }
        if (callActivity != null) {
            callActivity.runOnUiThread(() -> callActivity.onCasesUpdate(new ArrayList<>(cases.values())));
        }
    }

    public void handleCaseUpdate() {
        if (callActivity != null) {
            callActivity.runOnUiThread(() -> callActivity.onCasesUpdate(new ArrayList<>(cases.values())));
        }
    }

    public void startEmergency() {
        if (callingCase != null || acceptedCase != null || socket == null) {
            return;
        }
        socket.emit("startEmergency");

        asVictim = true;
        asVolunteer = false;
        isEmergencyStart = true;
    }

    public void stopEmergency() {
        if (!asVictim || callingCase == null) {
            return;
        }
        try {
            JSONObject jsonCase = new JSONObject(new Gson().toJson(callingCase, Case.class));
            socket.emit("stopEmergency", jsonCase);
            callingCase = null;
            asVictim = false;
            asVolunteer = false;
            isEmergencyStart = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopVolunteer() {
        if (!asVolunteer || acceptedCase == null) {
            return;
        }
        try {
            JSONObject jsonCase = new JSONObject(new Gson().toJson(acceptedCase, Case.class));
            socket.emit("stopVolunteer", jsonCase);
            asVolunteer = false;
            asVictim = false;
            acceptedCase = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void acceptVolunteer(String caseId) {
        if (asVolunteer || acceptedCase != null) {
            return;
        }
        Log.d(getClass().getSimpleName(), "Accept Volunteer");
        try {
            Case cs = getCase(caseId);
            JSONObject jsonCase;
            jsonCase = new JSONObject(new Gson().toJson(cs, Case.class));
            socket.emit("acceptVolunteer", jsonCase);
            asVolunteer = true;
            asVictim = false;
            acceptedCase = cs;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerView(NotifyFromVolunteerActivity view) {
        notifyFromVolunteerActivity = view;
    }

    public void unregisterView(NotifyFromVolunteerActivity view) {
        notifyFromVolunteerActivity = null;
    }

    public void registerView(EmergencyCaseActivity view) {
        emergencyCaseActivity = view;
    }

    public void unregisterEmergencyCaseActivity() {
        emergencyCaseActivity = null;
    }

    public void registerView(CallActivity view) {
        callActivity = view;
        callActivity.runOnUiThread(() -> callActivity.onCasesUpdate(new ArrayList<>(cases.values())));
    }

    public void unregisterCallActivity() {
        callActivity = null;
    }

    public void registerView(VolunteerActivity view) {
        volunteerActivity = view;
    }

    public void unregisterVolunteerActivity() {
        volunteerActivity = null;
    }

    public boolean isEmergencyStart() {
        return isEmergencyStart;
    }

    public boolean isAsVictim() {
        return asVictim;
    }

    public boolean isAsVolunteer() {
        return asVolunteer;
    }

    public Case getCase(String caseId) {
        return cases.get(caseId);
    }

    public Case getAcceptedCase() {
        return acceptedCase;
    }

    public Case getCallingCase() {
        return callingCase;
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
                headers.put("x-access-token", Collections.singletonList(AccountManagement.getUserAccessToken()));
            });
        });
        socket.on("caseCreated", onCaseCreated);
        socket.on("caseClosed", onCaseClosed);
        socket.on("volunteerAccept", onVolunteerAccept);
        socket.on("volunteerStop", onVolunteerStop);
        socket.on("victimCall", onVictimCall);
        socket.on("volunteerUpdate", onVolunteerUpdate);
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

    private Notification getVictimCallNotification(float distance, Case cs) {
        Intent intent = new Intent(this, EmergencyCaseActivity.class);
        intent.putExtra("caseId", cs.getId());
        PendingIntent viewCase = PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, "emergency-support-victim-call")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Có người cần trợ giúp gần bạn")
                .setContentText("Cách bạn " + String.format("%.1f", distance) + " m")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.mipmap.ic_launcher_round, "Xem thông tin", viewCase)
                .build();
    }

    private Notification getVolunteerAcceptNotification(String name, double distance) {
        return new NotificationCompat.Builder(this, "emergency-support-volunteer-accept")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Tình nguyện viên đang đến")
                .setContentText(name + " đang đến. Cách bạn " + String.format("%.1f", distance) + " m")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }
}
