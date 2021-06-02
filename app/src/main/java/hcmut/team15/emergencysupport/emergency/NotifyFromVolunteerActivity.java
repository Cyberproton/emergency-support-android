package hcmut.team15.emergencysupport.emergency;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.notificationCard.Notification;
import hcmut.team15.emergencysupport.notificationCard.notificationAdapter;

public class NotifyFromVolunteerActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Notification> exampleList;
    Button btn_stop;

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

    public void insertItem(View view) {
        exampleList.add(new Notification(R.drawable.ic_baseline_person_24, "Nguyễn Văn B", "0123456789"));
        mAdapter.notifyDataSetChanged();
    }

    public void stopSignal(View view) {
        Intent menu_intent = new Intent(NotifyFromVolunteerActivity.this, EmergencyActivity.class);
        startActivity(menu_intent);
    }
}