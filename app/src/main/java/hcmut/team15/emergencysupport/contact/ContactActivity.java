package hcmut.team15.emergencysupport.contact;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.R;
import hcmut.team15.emergencysupport.model.Contact;
import hcmut.team15.emergencysupport.model.ContactsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactActivity extends AppCompatActivity {
    private RecyclerView contactRecyclerView;
    private ContactAdapter contactAdapter;
    private RecyclerView.LayoutManager contactLayoutManager;


    private ContactInterface contactInterface = MainApplication.getInstance().getRetrofit().create(ContactInterface.class);

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ArrayList<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("a", "1", "0123456789"));
        contacts.add(new Contact("b", "1", "0123456789"));
        contacts.add(new Contact("c", "1", "0123456789"));

        contactRecyclerView = findViewById(R.id.contacts_recycle_view);
        contactRecyclerView.setHasFixedSize(true);
        contactLayoutManager = new LinearLayoutManager(this);
        contactAdapter = new ContactAdapter(contacts);
        contactRecyclerView.setLayoutManager(contactLayoutManager);
        contactRecyclerView.setAdapter(contactAdapter);

        contactAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemCLick(int position) {
                Contact contact = contacts.get(position);
                contact.setExpanded(!contact.isExpanded());
                contactAdapter.notifyItemChanged(position);
                /*
                contacts.get(position).setName("Click" + position);
                contactAdapter.notifyItemChanged(position);

                 */
            }

            @Override
            public void onCallCLick(int position) {
                Toast.makeText(getApplicationContext(), "Call", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onEditCLick(int position) {
                Toast.makeText(getApplicationContext(), "Edit", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDeleteCLick(int position) {
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*

        contactInterface.getContact(MainApplication.VICTIM_ACCESS, "60c96dc664f2d92db8f1f84c").enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                ContactsResponse res = response.body();
                Log.d("Single Contact", res.getContact().getName());
                Log.d("Single Contact", res.getContact().getPhone());
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });

        Map<String, String> body = new HashMap<>();
        body.put("name", "quan");
        body.put("phone", "0000000001");
        contactInterface.updateContact(MainApplication.VICTIM_ACCESS, "60c96dc664f2d92db8f1f84c", body).enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                ContactsResponse res = response.body();
                Log.d("Update Contact", res.getContact().getName());
                Log.d("Update Contact", res.getContact().getPhone());
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });

        Map<String, String> body1 = new HashMap<>();
        body1.put("name", "add");
        body1.put("phone", "0000000002");
        contactInterface.addContact(MainApplication.VICTIM_ACCESS, body1).enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                ContactsResponse res = response.body();
                Log.d("Add Contact", res.getContact().getName());
                Log.d("Add Contact", res.getContact().getPhone());
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });

        contactInterface.getContacts(MainApplication.VICTIM_ACCESS).enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                ContactsResponse res = response.body();
                Log.d("Contact", "" + (res == null));
                Log.d("Contact", "" + res.getContacts().size());
                Log.d("Contact", res.getContacts().get(0).getPhone());
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });

        contactInterface.deleteContact(MainApplication.VICTIM_ACCESS, "60c97f9864f2d92db8f2036a").enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Call<ContactsResponse> call, Response<ContactsResponse> response) {
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
                ContactsResponse res = response.body();
                Log.d("Delete Contact", "Success!");
                Log.d("Delete Contact", res.getContact().getName());
                Log.d("Delete Contact", res.getContact().getPhone());
            }

            @Override
            public void onFailure(Call<ContactsResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });
         */



    }
}
