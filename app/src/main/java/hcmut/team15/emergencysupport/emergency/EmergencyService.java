package hcmut.team15.emergencysupport.emergency;

import android.app.Notification;
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
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.call.CallActivity;
import hcmut.team15.emergencysupport.login.AccountManagement;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
import hcmut.team15.emergencysupport.model.User;
import hcmut.team15.emergencysupport.setting.ActivitySettings;
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
            EmergencyService.this.volunteerAcceptNotifications.put(volunteer.getUsername(), volunteerAcceptNotificationId);
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
                JSONObject jsonVolunteer = (JSONObject) args[0];
                User volunteer = new Gson().fromJson(jsonVolunteer.toString(), User.class);
                handleCaseUpdate(cs);
                if (this.notifyFromVolunteerActivity != null) {
                    this.notifyFromVolunteerActivity.onCaseClosed();
                }
                Integer notificationId = EmergencyService.this.volunteerAcceptNotifications.get(volunteer.getUsername());
                if (notificationId != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.cancel(notificationId);
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
        Log.d(this.getClass().getSimpleName(), "Case Created");
        try {
            JSONObject cs = (JSONObject) args[0];
            Gson gson = new Gson();
            this.callingCase = gson.fromJson(cs.toString(), Case.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onCaseClosed = args -> {
        Log.d(this.getClass().getSimpleName(), "Case Closed");
        try {
            JSONObject jsonCase = (JSONObject) args[0];
            Gson gson = new Gson();
            Case cs = gson.fromJson(jsonCase.toString(), Case.class);
            if (this.asVolunteer && this.acceptedCase != null && this.acceptedCase.getId().equals(cs.getId())) {
                this.asVolunteer = false;
                this.asVictim = false;
                this.acceptedCase = null;
                if (this.volunteerActivity != null) {
                    this.volunteerActivity.runOnUiThread(() -> this.volunteerActivity.onCaseClosed());
                }
            }
            this.cases.remove(cs.getId());
            Integer notificationId = EmergencyService.this.victimCallNotifications.get(cs.getId());
            if (notificationId != null) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancel(notificationId);
            }
            EmergencyService.this.victimCallNotifications.remove(cs.getId());
            handleCaseUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final Emitter.Listener onVictimCall = args -> {
        try {
            Log.d(EmergencyService.class.getSimpleName(), "Victim call");

            JSONObject victim = (JSONObject) args[0];
            JSONObject jsonCase = (JSONObject) args[1];
            JSONObject jsonYou = (JSONObject) args[2];

            User you = new Gson().fromJson(jsonYou.toString(), User.class);
            Case cs = new Gson().fromJson(jsonCase.toString(), Case.class);

            boolean shouldNotify = !this.cases.containsKey(cs.getId());
            handleCaseUpdate(cs);

            Log.d(EmergencyService.class.getSimpleName(), "Case received: " + jsonCase.toString());
            Log.d(getClass().getSimpleName(), "Should notify: " + shouldNotify);

            if (this.asVictim || this.asVolunteer) {
                return;
            }

            Log.d(getClass().getSimpleName(), "Check notify: " + shouldNotify);

            if (shouldNotify) {
                float dist = Location.distanceBetween(cs.getCaller().getCurrentLocation(), you.getCurrentLocation());
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(victimCallNotificationId, getVictimCallNotification(dist, cs));
                EmergencyService.this.victimCallNotifications.put(cs.getId(), victimCallNotificationId);
                victimCallNotificationId++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private final LocalBinder serviceBinder = new LocalBinder();
    private final Map<String, Case> cases = new HashMap<>();
    private final Map<String, Integer> victimCallNotifications = new HashMap<>();
    private final Map<String, Integer> volunteerAcceptNotifications = new HashMap<>();

    private Socket socket;
    private boolean isEmergencyStart;

    private Case acceptedCase;
    private boolean asVolunteer = false;

    private Case callingCase;
    private boolean asVictim = false;

    private static int victimCallNotificationId = 1000;
    private static int volunteerAcceptNotificationId = 2000;
    private static int emergencyNotificationId = 999;
    private NotifyFromVolunteerActivity notifyFromVolunteerActivity;
    private EmergencyCaseActivity emergencyCaseActivity;
    private CallActivity callActivity;
    private VolunteerActivity volunteerActivity;

    private int currentLedSignal = 0;

    @Override
    public void onCreate() {
        Log.d("EmergencyService", "Service created");
        super.onCreate();
        createSocket(getString(R.string.server_url));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(2, getForegroundNotification());
        }
    }

    public void createSocket(String serverUrl, boolean force) {
        if (AccountManagement.getUserAccessToken() == null) {
            Log.w("EmergencyService", "Unable to create socket because user token is null");
            return;
        }
        try {
            if (socket != null) {
                if (force) {
                    socket.disconnect();
                    socket = null;
                } else {
                    return;
                }
            }
            socket = IO.socket(serverUrl);
            handleSocket();
            socket.connect();
            Log.d("EmergencyService", "Socket IO Client connected to server with url=" + serverUrl);
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSocket() {
        createSocket(getString(R.string.server_url));
    }

    public void disconnectSocket() {
        stopEmergency();
        stopVolunteer();
        cases.clear();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        for (Map.Entry<String, Integer> entry : victimCallNotifications.entrySet()) {
            notificationManager.cancel(entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : volunteerAcceptNotifications.entrySet()) {
            notificationManager.cancel(entry.getValue());
        }
        if (socket == null) {
            return;
        }
        try {
            socket.disconnect();
            socket = null;
            Log.d("EmergencyService", "Socket IO Client disconnected from server");
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public void handleCaseUpdate(Case cs) {
        if (asVictim && callingCase != null && callingCase.getId().equals(cs.getId())) {
            this.callingCase = cs;
            if (callingCase.getVolunteers().isEmpty() && currentLedSignal != 1) {
                sendLedSignal(1);
            } else if (!callingCase.getVolunteers().isEmpty() && currentLedSignal != 2) {
                sendLedSignal(2);
            }
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
        if (volunteerActivity != null) {
            volunteerActivity.runOnUiThread(() -> volunteerActivity.onCaseUpdate());
        }
    }

    public void handleCaseUpdate() {
        if (callActivity != null) {
            callActivity.runOnUiThread(() -> callActivity.onCasesUpdate(new ArrayList<>(cases.values())));
        }
        if (volunteerActivity != null) {
            volunteerActivity.runOnUiThread(() -> volunteerActivity.onCaseUpdate());
        }
    }

    public void startEmergency() {
        if (callingCase != null || acceptedCase != null) {
            if (callingCase != null) {
                Log.w(EmergencyService.class.getSimpleName(), "Calling case is not null");
            }
            if (acceptedCase != null) {
                Log.w(EmergencyService.class.getSimpleName(), "Accepted case is not null");
            }
            return;
        }
        if (socket == null) {
            Log.w("EmergencyService", "Unable to start emergency, socket is null");
            return;
        }
        socket.emit("startEmergency");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(emergencyNotificationId, getEmergencyNotification());
        Log.i("EmergencyService", "Emergency Started");
        asVictim = true;
        asVolunteer = false;
        isEmergencyStart = true;
        sendLedSignal(1);
    }

    public void stopEmergency() {
        if (!asVictim || callingCase == null) {
            return;
        }
        if (socket == null) {
            Log.w("EmergencyService", "Unable to stop emergency, socket is null");
            return;
        }
        try {
            JSONObject jsonCase = new JSONObject(new Gson().toJson(callingCase, Case.class));
            socket.emit("stopEmergency", jsonCase);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(emergencyNotificationId);
            for (int nid : volunteerAcceptNotifications.values()) {
                notificationManager.cancel(nid);
            }
            cases.remove(callingCase.getId());
            callingCase = null;
            asVictim = false;
            asVolunteer = false;
            isEmergencyStart = false;
            sendLedSignal(0);
            Log.i("EmergencyService", "Emergency Stopped");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopVolunteer() {
        if (!asVolunteer || acceptedCase == null) {
            return;
        }
        if (socket == null) {
            Log.w("EmergencyService", "Unable to stop volunteer, socket is null");
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
        if (socket == null) {
            Log.w("EmergencyService", "Unable to accept volunteer, socket is null");
            return;
        }
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

    public Map<String, Case> getCases() {
        return cases;
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
                .setSmallIcon(R.drawable.ic_baseline_settings_input_antenna_24)
                .setContentTitle("Dịch vụ khẩn cấp đang chạy")
                .setContentText("Dịch vụ đang chạy ngầm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                //.addAction(R.mipmap.ic_launcher_round, "Tắt dịch vụ", stopServicePendingIntent)
                .build();
    }

    private Notification getVictimCallNotification(float distance, Case cs) {
        Intent intent = new Intent(this, VolunteerActivity.class);
        Log.d(EmergencyService.class.getSimpleName(), "Extra: " + cs.getId());
        intent.putExtra("caseId", cs.getId());
        try {
            String jsonCase = new Gson().toJson(cs, Case.class);
            intent.putExtra("case", jsonCase);
        } catch (Exception ex) { }
        PendingIntent viewCase = PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, "emergency-support-victim-call")
                .setSmallIcon(R.drawable.ic_baseline_warning_24)
                .setContentTitle("Có người cần trợ giúp gần bạn")
                .setContentText("Cách bạn " + String.format("%.1f", distance) + " m")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.mipmap.ic_launcher_round, "Xem thông tin", viewCase)
                .build();
    }

    private Notification getVolunteerAcceptNotification(String name, double distance) {
        return new NotificationCompat.Builder(this, "emergency-support-volunteer-accept")
                .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
                .setContentTitle("Tình nguyện viên đang đến")
                .setContentText(name + " đang đến. Cách bạn " + String.format("%.1f", distance) + " m")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private Notification getEmergencyNotification() {
        return new NotificationCompat.Builder(this, "emergency-support-volunteer-accept")
                .setSmallIcon(R.drawable.ic_baseline_settings_input_antenna_24)
                .setContentTitle("Bạn đang phát tín hiệu")
                .setContentText("Bạn đang phát tín hiệu")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void sendLedSignal(int data) {
        if (!ActivitySettings.isListenToButtonTrigger()) {
            return;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "1");
        jsonObject.addProperty("name", "LED");
        jsonObject.addProperty("data", "" + data);
        jsonObject.addProperty("unit", "");
        currentLedSignal = data;
        MainApplication.getInstance().getMqttService().sendLedData(jsonObject.toString());
    }
}
