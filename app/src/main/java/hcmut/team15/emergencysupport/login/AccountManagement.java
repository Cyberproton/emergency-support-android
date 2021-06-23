package hcmut.team15.emergencysupport.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import hcmut.team15.emergencysupport.MainApplication;

public class AccountManagement extends AppCompatActivity {
    static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";
    static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";
    static final String PREF_USER_LOGGED_IN_STATUS = "logged_in_status";
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
        editor.apply();
    }
}
