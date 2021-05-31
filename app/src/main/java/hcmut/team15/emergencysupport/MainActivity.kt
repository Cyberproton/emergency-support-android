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
import hcmut.team15.emergencysupport.emergency.EmergencyActivity
import hcmut.team15.emergencysupport.location.LocationService
import hcmut.team15.emergencysupport.register.RegisterInterface
import hcmut.team15.emergencysupport.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    // Access from emulator
    private var locationService: LocationService? = null
    private var registerInterface: RegisterInterface = MainApplication.getInstance().retrofit.create(RegisterInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val body = hashMapOf<String, String>()
            body["username"] = "test"
            body["password"] = "test"
            registerInterface.register(body).enqueue(object: Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    var response = response.body()!!
                    Log.d("MainActivity", response.message)
                    response.user
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {

                }

            })
        }

        val emergencyBtn = findViewById<Button>(R.id.egcy_button)
        emergencyBtn.setOnClickListener { view->
            val intent: Intent = Intent(this, EmergencyActivity::class.java);
            startActivity(intent)
        }


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