package hcmut.team15.emergencysupport.contact;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.login.AccountManagement;
import hcmut.team15.emergencysupport.model.Contact;
import hcmut.team15.emergencysupport.model.ContactsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactActivity extends AppCompatActivity {
    private RecyclerView contactRecyclerView;
    private ContactAdapter contactAdapter;
    private RecyclerView.LayoutManager contactLayoutManager;
    private ArrayList<Contact> contacts = new ArrayList<>(), contactss = new ArrayList<>();

    private SearchView contactSearchView;
    private Button contactAddBtn;
    private int REQ_ADD_CODE = -1;

    private ContactInterface contactInterface = MainApplication.getInstance().getRetrofit().create(ContactInterface.class);



    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);


        contactAddBtn = findViewById(R.id.contact_add_btn);
        contactSearchView = findViewById(R.id.contact_search);



        contacts.add(new Contact("Mặc định","","0"));
        contacts.add(new Contact("* Police","","113"));
        contacts.add(new Contact("* Ambulance","","115"));
        contacts.add(new Contact("Của tôi","","0"));
        contactss.addAll(contacts);


        contactRecyclerView = findViewById(R.id.contacts_recycle_view);
        contactRecyclerView.setHasFixedSize(true);
        contactLayoutManager = new LinearLayoutManager(this);
        contactAdapter = new ContactAdapter(contacts);
        contactRecyclerView.setLayoutManager(contactLayoutManager);
        contactRecyclerView.setAdapter(contactAdapter);

        contactInterface.getContacts(AccountManagement.getUserAccessToken()).enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                ContactsResponse res = response.body();
                if(res != null){
                    ArrayList<Contact> contactList = new ArrayList<>(res.getContacts());
                    contacts.addAll(contactList);
                    contactss.addAll(contactList);
                    contactAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d("ContactActivity", t.getMessage());
            }
        });



        setEvent();
        contactSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getFilter(newText);
                return false;
            }
        });

        contactSearchView.setOnCloseListener(() -> {
            contactAddBtn.setVisibility(View.VISIBLE);

            return false;
        });

        contactSearchView.setOnSearchClickListener(v -> {
            contactAddBtn.setVisibility(View.GONE);
        });


        contactAddBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ContactActivity.this, ContactAdd_UpdateActivity.class);
            startActivityForResult(intent, contacts.size());

        });
    }

    private void getFilter(String query) {
        contacts.clear();
        contactAdapter.notifyDataSetChanged();
        if(query.isEmpty()){
            contacts.addAll(contactss);
        }
        else{
            for(Contact contact : contactss){
                if(!contact.getPhone().equals("0")){
                    if(contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                       contact.getPhone().toLowerCase().contains(query.toLowerCase())) {
                        contacts.add(contact);
                    }
                }
            }
        }
        contactAdapter.notifyDataSetChanged();
    }

    private void setEvent() {
        contactAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemCLick(int position) {
                List<Integer> positions = new ArrayList<>();
                for (int i = 0; i < contacts.size(); i++) {
                    Contact contact = contacts.get(i);
                    if (i != position && contact.isExpanded()) {
                        contact.setExpanded(false);
                        positions.add(i);
                    }
                }
                Contact contact = contacts.get(position);
                contact.setExpanded(!contact.isExpanded());
                contactAdapter.notifyItemChanged(position);
                for (int i : positions) {
                    contactAdapter.notifyItemChanged(i);
                }
            }

            @Override
            public void onCallCLick(int position) {
                Contact contact = contacts.get(position);
                String phone = "+" + contact.getPhone();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);

            }

            @Override
            public void onEditCLick(int position) {
                edit(position);
            }

            @Override
            public void onDeleteCLick(int position) {
                confirmDeletion(position);
            }
        });
    }

    private void edit(int position) {

        Contact contact = contacts.get(position);
        boolean c = contact.getName().charAt(0) == '*';
        if(c){
            AlertDialog.Builder deleteAlertDialog = new AlertDialog.Builder(this);
            myownDialog(deleteAlertDialog, "Xóa số khỏi danh bạ", "Bạn không chỉnh sửa\nvì đây là số mặc định", "Tôi đã hiểu");
            deleteAlertDialog.show();
        }
        else{
            Intent intent = new Intent(ContactActivity.this, ContactAdd_UpdateActivity.class);
            intent.putExtra("editor", true);
            String name = contact.getName();
            String phone = contact.getPhone();
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            startActivityForResult(intent, position);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name"), phone = data.getStringExtra("phone");
            if (requestCode == contacts.size()) {

                Map<String, String> body1 = new HashMap<>();
                body1.put("name", name);
                body1.put("phone", phone);
                contactInterface.addContact(AccountManagement.getUserAccessToken(), body1).enqueue(new Callback<ContactsResponse>() {
                    @Override
                    public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                        ContactsResponse res = response.body();
                        Contact contact = res.getContact();

                        contacts.add(contact);
                        contactAdapter.notifyItemInserted(contacts.size());
                        contactss.add(contact);
                    }

                    @Override
                    public void onFailure(Call<ContactsResponse> call, Throwable t) {
                        Log.d("ContactActivity", t.getMessage());
                    }
                });

            } else {
                Contact contact = contacts.get(requestCode);

                Map<String, String> body = new HashMap<>();
                body.put("name", name);
                body.put("phone", phone);
                contactInterface.updateContact(AccountManagement.getUserAccessToken(), contact.get_id(), body).enqueue(new Callback<ContactsResponse>() {
                    @Override
                    public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                        ContactsResponse res = response.body();
                        Contact contact = contacts.get(requestCode);
                        int initialPos = contactss.indexOf(contact);
                        contact.setName(res.getContact().getName());
                        contact.setPhone(res.getContact().getPhone());
                        contactAdapter.notifyItemChanged(requestCode);

                        Contact initialContact = contactss.get(initialPos);
                        initialContact.setName(res.getContact().getName());
                        initialContact.setPhone(res.getContact().getPhone());
                    }

                    @Override
                    public void onFailure(Call<ContactsResponse> call, Throwable t) {
                        Log.d("ContactActivity", t.getMessage());
                    }
                });

            }
        }
    }

    public void confirmDeletion(int position){
        AlertDialog.Builder deleteAlertDialog = new AlertDialog.Builder(this);
        Contact contact = contacts.get(position);

        String name = contact.getName();
        boolean c = name.charAt(0) == '*';
        if(c){
            myownDialog(deleteAlertDialog, "Xóa số khỏi danh bạ", "Bạn không thể xóa \nvì đây là số mặc định", "Tôi đã hiểu");
        }
        else{
            String title = "Xoá số khỏi danh bạ", message =  "Tên: " + contact.getName() + "\nSĐT: " + contact.getPhone(), closeTitle = "Hủy bỏ";
            int initialPos = contactss.indexOf(contact);
            myownDialog(deleteAlertDialog, title, message, closeTitle);

            deleteAlertDialog.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    contactInterface.deleteContact(AccountManagement.getUserAccessToken(), contact.get_id()).enqueue(new Callback<ContactsResponse>() {
                        @Override
                        public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                            //không chạy
                            Log.d("delete contact", "success!");

                        }

                        @Override
                        public void onFailure(Call<ContactsResponse> call, Throwable t) {
                            Log.d("ContactActivity", t.getMessage());
                        }
                    });
                    contacts.remove(position);
                    contactAdapter.notifyItemRemoved(position);
                    contactss.remove(initialPos);
                }
            });
        }
        deleteAlertDialog.show();
    }

    private void myownDialog(AlertDialog.Builder deleteAlertDialog, String title, String message, String closeTitle) {
        deleteAlertDialog.setTitle(title);
        deleteAlertDialog.setMessage(message);
        deleteAlertDialog.setNegativeButton(closeTitle, (dialog, which) -> dialog.cancel());
    }

}
