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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    // Access from emulator
    private var locationService: LocationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener( OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("messaging-service", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                Log.d("messaging-service", "token=$token")
                Toast.makeText(baseContext, "token=$token", Toast.LENGTH_SHORT).show()
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), -1)
            }
        }
    }
}