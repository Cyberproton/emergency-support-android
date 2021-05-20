package hcmut.team15.emergencysupport

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    // Access from emulator
    private val baseUrl = "http://10.0.2.2:3000/"
    private val mainRoute: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val helloInterface: HelloInterface = mainRoute.create(HelloInterface::class.java)
    private lateinit var buttonTriggerService: MqttService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helloButton = findViewById<Button>(R.id.hello)
        helloButton.setOnClickListener { view ->
            val call = helloInterface.executeGetHello()
            call.enqueue(object: Callback<HelloResponse> {
                override fun onResponse(call: Call<HelloResponse>, response: Response<HelloResponse>) {
                    Toast.makeText(
                        this@MainActivity,
                        "Response from server: ${response.body()?.helloMessage}",
                        Toast.LENGTH_LONG,
                    ).show()
                }

                override fun onFailure(call: Call<HelloResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            })
        }

        val helloPostButton = findViewById<Button>(R.id.posthello)
        helloPostButton.setOnClickListener {
            handleHelloDialog()
        }

        buttonTriggerService = MqttService(this, "cyberproton/feeds/button")
        buttonTriggerService.setCallback(object: MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.w("mqtt-connection", "Disconnected from server")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val m = message.toString()
                if (m == "1") {
                    Toast.makeText(
                        this@MainActivity,
                        "Message from Adafruit: topic=$topic, message=${message.toString()}, Button pressed",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.w("mqtt-connection", serverURI?:"Server URI not found")
            }

        })

        val emergencyBtn = findViewById<Button>(R.id.egcy_button)
        emergencyBtn.setOnClickListener { view->
            val intent: Intent = Intent(this, EmergencyActivity::class.java);
            startActivity(intent)
        }


    }

    private fun handleHelloDialog() {
        val view = layoutInflater.inflate(R.layout.hello_dialog, null)
        val dialog = AlertDialog.Builder(this)
        dialog.setView(view).show()
        val nameInput = view.findViewById<EditText>(R.id.nameInput)
        val nameSubmitButton = view.findViewById<Button>(R.id.submitName)

        nameSubmitButton.setOnClickListener { view ->
            val call = helloInterface.executePostHello(mapOf("helloMessage" to nameInput.text.toString()))
            call.enqueue(object: Callback<HelloResponse> {
                override fun onResponse(call: Call<HelloResponse>, response: Response<HelloResponse>) {
                    Toast.makeText(
                        this@MainActivity,
                        "Response from server: ${response.body()?.helloMessage}",
                        Toast.LENGTH_LONG,
                    ).show()
                }

                override fun onFailure(call: Call<HelloResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            })
        }
    }
}