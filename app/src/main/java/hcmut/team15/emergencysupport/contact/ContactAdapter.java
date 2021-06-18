package hcmut.team15.emergencysupport.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Contact;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private ArrayList<Contact> contacts;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void onItemCLick(int position);
        void onCallCLick(int position);
        void onEditCLick(int position);
        void onDeleteCLick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public ContactAdapter(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView contactName, contactPhone;
        ImageView callView, editView, deleteView;
        LinearLayout contactExpandableLayout;

        public ContactViewHolder(@NonNull @NotNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            callView = itemView.findViewById(R.id.contact_call);
            editView = itemView.findViewById(R.id.contact_edit);
            deleteView = itemView.findViewById(R.id.contact_delete);
            contactExpandableLayout = itemView.findViewById(R.id.contact_expandable_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            itemClickListener.onItemCLick(position);
                        }
                    }
                }
            });

            callView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            itemClickListener.onCallCLick(position);
                        }
                    }
                }
            });

            editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            itemClickListener.onEditCLick(position);
                        }
                    }
                }
            });

            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            itemClickListener.onDeleteCLick(position);
                        }
                    }
                }
            });


        }
    }

    @NonNull
    @NotNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        ContactViewHolder contactViewHolder = new ContactViewHolder(v, itemClickListener);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContactViewHolder holder, int position) {
        Contact currentItem = contacts.get(position);
        holder.contactName.setText(currentItem.getName());
        holder.contactPhone.setText(currentItem.getPhone());

        boolean isExpanded = currentItem.isExpanded();
        holder.contactExpandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }




}
