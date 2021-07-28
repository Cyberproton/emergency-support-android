package hcmut.team15.emergencysupport.contact;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Contact;


public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int TYPE_CONTACT = 1;
    private static int TYPE_CONTACT_HEADER = 2;
    private static int HEADER1 = 0, HEADER2 = 4;

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

            itemView.setOnClickListener(v -> {
                if(itemClickListener != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        itemClickListener.onItemCLick(position);
                    }
                }
            });

            callView.setOnClickListener(v -> {
                if(itemClickListener != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        itemClickListener.onCallCLick(position);
                    }
                }
            });

            editView.setOnClickListener(v -> {
                if(itemClickListener != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        itemClickListener.onEditCLick(position);
                    }
                }
            });

            deleteView.setOnClickListener(v -> {
                if(itemClickListener != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        itemClickListener.onDeleteCLick(position);
                    }
                }
            });


        }
    }

    public static class ContactHeaderViewHolder extends RecyclerView.ViewHolder{
        private TextView contactHeader;

        public ContactHeaderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            contactHeader = itemView.findViewById(R.id.contact_header);
        }
    }


    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_CONTACT){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
            ContactViewHolder contactViewHolder = new ContactViewHolder(v, itemClickListener);
            return contactViewHolder;
        }
        else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_header_item, parent, false);
            ContactHeaderViewHolder contactHeaderViewHolder = new ContactHeaderViewHolder(v);
            return contactHeaderViewHolder;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        if(holder.getItemViewType() == TYPE_CONTACT){
            ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
            Contact currentItem = contacts.get(position);

                contactViewHolder.contactName.setText(currentItem.getName());
                contactViewHolder.contactPhone.setText(currentItem.getPhone());
                /*
                Log.d("phone" , ""  + currentItem.getPhone());
                Log.d("phone" , ""  + currentItem.getName());
                Log.d("phone" , ""  + currentItem.getPhone().charAt(0));
                String phone = currentItem.getPhone();

                 */
                boolean isExpanded = currentItem.isExpanded();

                contactViewHolder.contactExpandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                /*
                boolean flag = phone.length() == 3;
                if(flag){
                    Log.d("hide" , "" + flag);
                    contactViewHolder.editView.setVisibility(View.GONE);
                    contactViewHolder.deleteView.setVisibility(View.GONE);
                }

                 */

        }
        else{
            Contact currentItem = contacts.get(position);
            ContactHeaderViewHolder contactHeaderViewHolder = (ContactHeaderViewHolder) holder;
            contactHeaderViewHolder.contactHeader.setText(currentItem.getName());
        }
    }

    @Override
    public int getItemCount() {
        if(contacts != null) return contacts.size();
        return 0;
    }


    @Override
    public int getItemViewType(int position) {
        Contact contact = contacts.get(position);
        if(isHeader(contact.getPhone())) return TYPE_CONTACT_HEADER;
        return TYPE_CONTACT;
    }

    private boolean isHeader(String phone) {
        return phone.equals("0");
    }
}
