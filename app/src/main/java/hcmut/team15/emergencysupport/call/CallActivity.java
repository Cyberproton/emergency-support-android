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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.MenuActivity;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.emergency.EmergencyService;
import hcmut.team15.emergencysupport.emergency.VolunteerActivity;
import hcmut.team15.emergencysupport.login.AccountManagement;
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
    private TextView allCallsLabel;
    private TextView currentCallLabel;
    private ConstraintLayout currentCallLayout;
    private TextView callPersonPhoneLabel;
    private TextView callPersonNameLabel;
    private TextView callPersonDistanceLabel;

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

        allCallsLabel = findViewById(R.id.all_calls_label);
        currentCallLabel = findViewById(R.id.current_calls_label);
        currentCallLayout = findViewById(R.id.call_card_layout_red);
        callPersonPhoneLabel = findViewById(R.id.call_person_phone_label_red);
        callPersonNameLabel = findViewById(R.id.call_person_name_label_red);
        callPersonDistanceLabel = findViewById(R.id.call_person_distance_label_red);
        currentCallLabel.setVisibility(View.GONE);
        currentCallLayout.setVisibility(View.GONE);

        Button menuButton = findViewById(R.id.call_menu_btn);
        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        calls.clear();
        callAdapter.notifyDataSetChanged();
        bindService(new Intent(this, EmergencyService.class), emergencyServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(emergencyServiceConnection);
        calls.clear();
        callAdapter.notifyDataSetChanged();
        super.onStop();
    }

    public void onCasesUpdate(List<Case> cases) {
        Case acceptedCase = emergencyService.getAcceptedCase();
        if (acceptedCase == null) {
            currentCallLabel.setVisibility(View.GONE);
            currentCallLayout.setVisibility(View.GONE);
            callPersonNameLabel.setText("");
            callPersonPhoneLabel.setText("");
            callPersonDistanceLabel.setText("? m");
        } else {
            currentCallLabel.setVisibility(View.VISIBLE);
            currentCallLayout.setVisibility(View.VISIBLE);
            User victim = acceptedCase.getCaller();
            callPersonNameLabel.setText(victim.getUsername());
            callPersonPhoneLabel.setText("");
            String distance = "? m";
            Location currentLocation = null;
            if (MainApplication.getInstance().getLocationService() != null) {
                currentLocation = new Location(MainApplication.getInstance().getLocationService().getLastLocation());
            }
            if (currentLocation != null && victim.getCurrentLocation() != null) {
                distance = String.format(Locale.getDefault(), "%.1f", Location.distanceBetween(victim.getCurrentLocation(), currentLocation));
                distance += "m";
            }
            callPersonDistanceLabel.setText(distance);
        }

        calls.clear();
        boolean hasCall = false;
        for (Case cs : cases) {
            User victim = cs.getCaller();
            if (victim.getUsername().equals(AccountManagement.getUsername())) {
                continue;
            }
            hasCall = true;
            String distance = "? m";
            Location currentLocation = null;
            if (MainApplication.getInstance().getLocationService() != null) {
                currentLocation = new Location(MainApplication.getInstance().getLocationService().getLastLocation());
            }
            if (currentLocation != null && victim.getCurrentLocation() != null) {
                distance = String.format(Locale.getDefault(), "%.1f", Location.distanceBetween(victim.getCurrentLocation(), currentLocation));
                distance += "m";
            }
            calls.add(new Call(R.drawable.ic_baseline_person_24, cs.getId(), cs, victim.getUsername(), "", distance));
        }
        if (hasCall) {
            allCallsLabel.setText("Tất cả:");
            allCallsLabel.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        } else {
            allCallsLabel.setText("Có vẻ không có ai cả");
            allCallsLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        callAdapter.notifyDataSetChanged();
    }

    private static class Call {
        private int imageRes;
        private String caseId;
        private Case cs;
        private String name;
        private String phone;
        private String distance;

        public Call(int imageRes, String caseId, Case cs, String name, String phone, String distance) {
            this.imageRes = imageRes;
            this.caseId = caseId;
            this.cs = cs;
            this.name = name;
            this.phone = phone;
            this.distance = distance;
        }

        public String getCaseId() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }

        public Case getCase() {
            return cs;
        }

        public void setCase(Case cs) {
            this.cs = cs;
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
            holder.view.setOnClickListener(v -> {
                Log.d(getClass().getSimpleName(), "Case View: " + call.getCaseId());
                Intent intent = new Intent(v.getContext(), VolunteerActivity.class);
                intent.putExtra("caseId", call.getCaseId());
                try {
                    Case cs = call.getCase();
                    String jsonCase = new Gson().toJson(cs, Case.class);
                    intent.putExtra("case", jsonCase);
                } catch (Exception ex) { }
                try {
                    String jsonCase = new Gson().toJson(call.getCase(), Case.class);
                    intent.putExtra("case", jsonCase);
                } catch (Exception ex) { }
                v.getContext().startActivity(intent);
            });
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
            imageView = itemView.findViewById(R.id.call_person_image_red);
            nameView = itemView.findViewById(R.id.call_person_name_label_red);
            phoneView = itemView.findViewById(R.id.call_person_phone_label_red);
            distanceView = itemView.findViewById(R.id.call_person_distance_label_red);
        }
    }
}
