package hcmut.team15.emergencysupport.emergency;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
import hcmut.team15.emergencysupport.model.User;
import hcmut.team15.emergencysupport.notificationCard.Notification;
import hcmut.team15.emergencysupport.notificationCard.notificationAdapter;

public class NotifyFromVolunteerActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Notification> exampleList;
    private Button btn_stop;
    private List<User> volunteers = new ArrayList<>();
    private Case cs;
    private EmergencyService emergencyService;
    private ServiceConnection emergencyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emergencyService = ((EmergencyService.LocalBinder) service).getService();
            emergencyService.registerView(NotifyFromVolunteerActivity.this);
            emergencyService.startEmergency();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (emergencyService.isEmergencyStart()) {
                emergencyService.stopEmergency();
                emergencyService.unregisterView(NotifyFromVolunteerActivity.this);
            }
            emergencyService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_from_volunteer);

        exampleList = new ArrayList<>();
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, "Nguyễn Văn B", "012345678"));
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, "Nguyễn Văn A", "012356781"));

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new notificationAdapter(exampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        Log.d(getClass().getSimpleName(), "Activity Started");
        super.onStart();
        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(getClass().getSimpleName(), "Activity Stopped");
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void insertItem(User volunteer, float distance) {
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, volunteer.getUsername(), String.format("%.2f", distance) + "m"));
        mAdapter.notifyDataSetChanged();
    }

    public void removeItem(User volunteer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exampleList.removeIf(n -> n.getText1().equals(volunteer.getUsername()));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void insertItem(View view) {
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, "test", "0123456789"));
        mAdapter.notifyDataSetChanged();
    }

    public void stopSignal(View view) {
        emergencyService.stopEmergency();
        Intent menu_intent = new Intent(NotifyFromVolunteerActivity.this, EmergencyActivity.class);
        startActivity(menu_intent);
    }

    public void updateCase(Case updated) {
        cs = updated;
        exampleList.clear();
        for (User volunteer : cs.getVolunteers()) {
            float distance = Location.distanceBetween(cs.getCaller().getCurrentLocation(), volunteer.getCurrentLocation());
            exampleList.add(new Notification(R.drawable.ic_baseline_person_24, volunteer.getUsername(), String.format("%.2f", distance) + "m"));
        }
        mAdapter.notifyDataSetChanged();
    }

    public void onCaseClosed() {
        CharSequence message = "Cuộc trợ giúp đã kết thúc, cảm ơn bạn đã tham gia";
        Snackbar sb = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        sb.show();
    }
}