package hcmut.team15.emergencysupport.call;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.emergency.EmergencyService;
import hcmut.team15.emergencysupport.emergency.VolunteerActivity;
import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.Location;
import hcmut.team15.emergencysupport.model.User;

public class CallActivity extends AppCompatActivity {
    private RecyclerView callRecycleView;
    private RecyclerView.Adapter<CallViewHolder> callAdapter;
    private RecyclerView.LayoutManager callLayoutManager;
    private List<Call> calls;
    private EmergencyService emergencyService;
    private final ServiceConnection emergencyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emergencyService = ((EmergencyService.LocalBinder) service).getService();
            emergencyService.registerView(CallActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            emergencyService.unregisterCallActivity();
            emergencyService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_calls);
        callRecycleView = findViewById(R.id.calls_recycle_view);
        calls = new ArrayList<>();
        callAdapter = new CallAdapter(calls, this);
        callLayoutManager = new LinearLayoutManager(this);
        callRecycleView.setAdapter(callAdapter);
        callRecycleView.setLayoutManager(callLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        super.onStop();
    }

    public void onCasesUpdate(List<Case> cases) {
        calls.clear();
        User you = MainApplication.you;
        for (Case cs : cases) {
            User victim = cs.getCaller();
            if (you.getUsername().equals(victim.getUsername())) {
                continue;
            }
            String distance = "Không rõ khoảng cách";
            if (you.getCurrentLocation() != null && victim.getCurrentLocation() != null) {
                distance = String.format("%.1f", Location.distanceBetween(victim.getCurrentLocation(), you.getCurrentLocation()));
                distance += "km";
            }
            calls.add(new Call(R.drawable.ic_baseline_person_24, victim.getUsername(), "", distance));
        }
        calls.add(new Call(R.drawable.ic_baseline_person_24, "test", "test", "10km"));
        callAdapter.notifyDataSetChanged();
    }

    private static class Call {
        private int imageRes;
        private String name;
        private String phone;
        private String distance;

        public Call(int imageRes, String name, String phone, String distance) {
            this.imageRes = imageRes;
            this.name = name;
            this.phone = phone;
            this.distance = distance;
        }

        public int getImageRes() {
            return imageRes;
        }

        public void setImageRes(int imageRes) {
            this.imageRes = imageRes;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }
    }

    private static class CallAdapter extends RecyclerView.Adapter<CallViewHolder> {
        private final List<Call> calls;
        private final Context context;

        public CallAdapter(List<Call> calls, Context context) {
            this.calls = calls;
            this.context = context;
        }

        @NonNull
        @NotNull
        @Override
        public CallViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.person_list_row, parent, false);
            return new CallViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull CallViewHolder holder, int position) {
            Call call = calls.get(position);
            holder.imageView.setImageResource(call.getImageRes());
            holder.nameView.setText(call.getName());
            holder.phoneView.setText(call.getPhone());
            holder.distanceView.setText(call.getDistance());
        }

        @Override
        public int getItemCount() {
            return calls.size();
        }
    }

    private static class CallViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView phoneView;
        private final TextView distanceView;

        public CallViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            view = itemView;
            imageView = itemView.findViewById(R.id.call_person_image);
            nameView = itemView.findViewById(R.id.call_person_name_label);
            phoneView = itemView.findViewById(R.id.call_person_phone_label);
            distanceView = itemView.findViewById(R.id.call_person_distance_label);

            itemView.setOnClickListener(view -> {
                Log.d(getClass().getSimpleName(), nameView.getText() + "clicked");
                Intent intent = new Intent(view.getContext(), VolunteerActivity.class);
                view.getContext().startActivity(intent);
            });
        }
    }
}
