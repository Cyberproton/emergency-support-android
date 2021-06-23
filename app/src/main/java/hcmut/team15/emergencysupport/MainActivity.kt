package hcmut.team15.emergencysupport

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import hcmut.team15.emergencysupport.contact.ContactActivity
import hcmut.team15.emergencysupport.emergency.EmergencyActivity
import hcmut.team15.emergencysupport.emergency.HelpRequestInterface
import hcmut.team15.emergencysupport.location.LocationService
import hcmut.team15.emergencysupport.login.LoginActivity
import hcmut.team15.emergencysupport.model.Case
import hcmut.team15.emergencysupport.model.HelpResponse
import hcmut.team15.emergencysupport.register.RegisterInterface
import hcmut.team15.emergencysupport.model.RegisterResponse
import hcmut.team15.emergencysupport.model.User
import hcmut.team15.emergencysupport.login.TokenVar
import hcmut.team15.emergencysupport.profile.ProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    // Access from emulator
    private var locationService: LocationService? = null
    private var registerInterface: RegisterInterface = MainApplication.getInstance().retrofit.create(RegisterInterface::class.java)
    private var helpInterface = MainApplication.getInstance().retrofit.create(HelpRequestInterface::class.java)
    private var ledColor = 1

    private var case: Case? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val data = JsonObject()
            data.addProperty("id", "1")
            data.addProperty("name", "LED")
            data.addProperty("data", "" + ledColor)
            data.addProperty("unit", "")
            ledColor = (ledColor + 1) % 3

            Log.d("MainActivity", "Sending $data")
            MainApplication.getInstance().mqttService.sendData("CSE_BBC/feeds/bk-iot-led", data.toString());

        }

        val emergencyBtn = findViewById<Button>(R.id.egcy_button)
        emergencyBtn.setOnClickListener { view->
            val intent: Intent = Intent(this, EmergencyActivity::class.java);
            startActivity(intent)
        }

        findViewById<Button>(R.id.asVictimBtn).setOnClickListener {
            MainApplication.VICTIM_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidmljdGltIiwiaWF0IjoxNjIxNTg4MDk1LCJleHAiOjE2NTMxMjQwOTV9.cVbIAbUEjaxCF_dRTEJCLKQBt4PXk8UNGZEvISVnW3Q";
            //MainApplication.you = User(MainApplication.VICTIM_USERNAME, "", null)
        }

        findViewById<Button>(R.id.asVolunteerBtn).setOnClickListener {
            MainApplication.VICTIM_ACCESS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoidm9sdW50ZWVyIiwiaWF0IjoxNjIxNTg4MDE2LCJleHAiOjE2NTMxMjQwMTZ9.UeeHdK07SWhVXb4oiND_kSCdiff-ZRFcb6RZXElIt5Q";
            //MainApplication.you = User(MainApplication.VOLUNTEER_USERNAME, "", null)
        }

        findViewById<Button>(R.id.asRealDevice).setOnClickListener {
            MainApplication.BACKEND_URI = MainApplication.REAL_URI;
            MainApplication.getInstance().emergencyService.createSocket(MainApplication.BACKEND_URI);
            MainApplication.getInstance().createRetrofit(MainApplication.BACKEND_URI)
            MainApplication.getInstance().locationService.createLocationRequestInterface();
        }

        findViewById<Button>(R.id.contactBtn).setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.profile_btn).setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    fun callForHelp() {

    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "Start")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Bind Location Service")
                handleLocationService()
            } else {
                Log.d("MainActivity", "Request permission for Location Service")
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Bind Location Service")
                //handleLocationService()
            } else {
                Log.d("MainActivity", "Request permission for Location Service")
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    private fun handleLocationService() {
        bindService(Intent(this, LocationService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                locationService = (service as? LocationService.LocalBinder)?.service
                locationService?.requestLocationUpdates()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                locationService?.removeLocationUpdates()
                locationService = null
            }
        }, BIND_AUTO_CREATE)
    }
}