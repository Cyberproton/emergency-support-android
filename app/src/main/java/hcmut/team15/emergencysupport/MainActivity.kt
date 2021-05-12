package hcmut.team15.emergencysupport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    // Access from emulator
    private val baseUrl = "http://10.0.2.2:3000"
    private val mainRoute: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val helloInterface: HelloInterface = mainRoute.create(HelloInterface::class.java)

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
    }

    fun handleHelloDialog() {
        val view = layoutInflater.inflate(R.layout.hello_dialog, null)
        val dialog = AlertDialog.Builder(this)
        dialog.setView(view).show()
        val nameInput = view.findViewById<EditText>(R.id.nameInput)
        val nameSubmitButton = view.findViewById<Button>(R.id.submitName)
        println("dsfsdfdfdsf")
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