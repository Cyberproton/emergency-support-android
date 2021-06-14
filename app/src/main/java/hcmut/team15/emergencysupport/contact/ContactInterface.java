package hcmut.team15.emergencysupport.contact;

import hcmut.team15.emergencysupport.model.ContactsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ContactInterface {
    // http:localhost:3000/api/contact/
    // GET: toan bo danh ba
    // POST: them danh ba

    // http:localhost:3000/api/contact/<contact-id>
    // GET: lay danh ba su dung id
    // PUT: cap nhat danh ba
    // DELETE: xoa danh ba

    @GET("/api/contact")
    Call<ContactsResponse> getContacts(@Header("x-access-token") String token);

    @GET("/api/contact/{id}")
    Call<ContactsResponse> getContact(@Header("x-access-token") String token, @Path("id") String contactId);
}
