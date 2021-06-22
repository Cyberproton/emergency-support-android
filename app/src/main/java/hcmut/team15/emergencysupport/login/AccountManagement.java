package hcmut.team15.emergencysupport.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class AccountManagement extends AppCompatActivity {
    static final String ACCESS_TOKEN = TokenVar.AccessToken;
    static final String PREF_USER_LOGGED_IN_STATUS = "logged_in_status";
    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    public static void setPrefLoggedInUserEmail(Context ctx, String tokenVar){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(ACCESS_TOKEN, tokenVar);
        editor.commit();
    }
    public static String getLoggedInEmailUser(Context ctx){
        return getSharedPreferences(ctx).getString(ACCESS_TOKEN, "");
    }
    public static void setUserLoggedInStatus(Context ctx, boolean status){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_USER_LOGGED_IN_STATUS, status);
        editor.commit();
    }
    public static boolean getUserLoggedInStatus(Context ctx){
        return getSharedPreferences(ctx).getBoolean(PREF_USER_LOGGED_IN_STATUS, false);
    }
    public static void clearLoggedInEmailAddress(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(ACCESS_TOKEN);
        editor.remove(PREF_USER_LOGGED_IN_STATUS);
        editor.commit();
    }
}
