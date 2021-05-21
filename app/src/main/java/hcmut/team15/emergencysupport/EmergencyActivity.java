package hcmut.team15.emergencysupport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class EmergencyActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

    }

    public void replaceFragment(View v){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
       switch (v.getId()){
           case R.id.signal_button: fragment = new Emergency_fragment1();
               break;
           case R.id.stop_button: fragment = new Emergency_fragment2();
               break;
       }

       fragmentTransaction.replace(R.id.framecontent, fragment);
       fragmentTransaction.commit();
    }
}



