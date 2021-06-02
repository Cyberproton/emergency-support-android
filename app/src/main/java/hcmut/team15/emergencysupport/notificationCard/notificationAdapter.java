package hcmut.team15.emergencysupport.notificationCard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

import hcmut.team15.emergencysupport.R;

public class notificationAdapter extends RecyclerView.Adapter<notificationAdapter.NotificationViewHolder> {
    private ArrayList<Notification> mNotifications;


    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_avatar;
        public TextView tv_name;
        public TextView tv_phoneNumber;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            iv_avatar = itemView.findViewById(R.id.noti_img);
            tv_name = itemView.findViewById(R.id.noti_name);
            tv_phoneNumber = itemView.findViewById(R.id.noti_phonenum);
        }
    }

    public notificationAdapter(ArrayList<Notification> notifications){
        mNotifications = notifications;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        NotificationViewHolder evh = new NotificationViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        Notification currentItem = mNotifications.get(position);
        holder.iv_avatar.setImageResource(currentItem.getImageResource());
        holder.tv_name.setText(currentItem.getText1());
        holder.tv_phoneNumber.setText(currentItem.getText2());
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }
}
