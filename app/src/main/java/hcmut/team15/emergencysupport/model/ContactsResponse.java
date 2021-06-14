package hcmut.team15.emergencysupport.model;

import java.util.List;

public class ContactsResponse {
    private List<Contact> contacts;
    private Contact contact;
    private String error;

    public ContactsResponse(List<Contact> contacts, String error) {
        this.contacts = contacts;
        this.error = error;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
