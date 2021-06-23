package hcmut.team15.emergencysupport.login;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.MainApplication;
import hcmut.team15.emergencysupport.model.User;

public class AccountManagement extends AppCompatActivity {
    static final String ACCESS_TOKEN = "login_access_token";
    static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";
    static final String PREF_USER_LOGGED_IN_STATUS = "logged_in_status";
    static final String USERNAME = "username";
    private static User user = new User();

    public static String getUserAccessToken() {
        return getSharedPreferences().getString(ACCESS_TOKEN, null);
    }

    public static SharedPreferences getSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
    }
    public static void setPrefLoggedInUserEmail(String tokenVar){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(ACCESS_TOKEN, tokenVar);
        editor.apply();
    }
    public static String getLoggedInEmailUser(){
        return getSharedPreferences().getString(ACCESS_TOKEN, "");
    }
    public static void setUserLoggedInStatus(boolean status){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREF_USER_LOGGED_IN_STATUS, status);
        editor.apply();
    }
    public static boolean getUserLoggedInStatus(){
        return getSharedPreferences().getBoolean(PREF_USER_LOGGED_IN_STATUS, false);
    }
    public static void clearLoggedInState(){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(ACCESS_TOKEN);
        editor.putString(ACCESS_TOKEN, DEFAULT_TOKEN);
        editor.remove(PREF_USER_LOGGED_IN_STATUS);
        editor.remove(USERNAME);
        editor.apply();
    }

    public static void setUser(User user) {
        if (user == null) {
            return;
        }
        AccountManagement.user = user;
    }

    public static User getUser() {
        return user;
    }

    public static void setUsername(String username) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(USERNAME, username);
        editor.apply();
    }

    public static String getUsername() {
        return getSharedPreferences().getString(USERNAME, null);
    }
}
