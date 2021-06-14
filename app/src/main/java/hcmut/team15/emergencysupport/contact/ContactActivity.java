package hcmut.team15.emergencysupport.contact;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.model.ContactsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactActivity extends AppCompatActivity {
    private ContactInterface contactInterface = MainApplication.getInstance().getRetrofit().create(ContactInterface.class);

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Contact", "Contact start");
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

        contactInterface.getContact(MainApplication.VICTIM_ACCESS, "60c6d5b35776b42b17596841").enqueue(new Callback<ContactsResponse>() {
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
    }
}
