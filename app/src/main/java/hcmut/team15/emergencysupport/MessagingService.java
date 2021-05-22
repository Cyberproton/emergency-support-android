package hcmut.team15.emergencysupport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        Log.d("MessagingService", "Received message " + remoteMessage.getMessageId());
        for (Map.Entry<String, String> e : remoteMessage.getData().entrySet()) {
            Log.d("MessagingService", e.getKey() + ":" + e.getValue());
        }
        String volunteer = remoteMessage.getData().get("volunteer");
        if (volunteer != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getBaseContext(), "Volunteer " + volunteer + " have accepted your help request", Toast.LENGTH_LONG).show());
            MainApplication.getInstance().getMqttService().sendData("cyberproton/feeds/led", "1");
        }
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
    }
}
