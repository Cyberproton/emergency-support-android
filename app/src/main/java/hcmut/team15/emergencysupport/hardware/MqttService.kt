package hcmut.team15.emergencysupport.hardware

import android.content.Context
import android.util.Log
import hcmut.team15.emergencysupport.R
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttService(context: Context, val subscriptionTopic: String) {
    val serverUri = "tcp://io.adafruit.com:1883"
    val username = "CSE_BBC"
    val password = context.getString(R.string.adafruit_bbc_password)
    val clientId = "9999"
    val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)

    init {
        mqttAndroidClient.setCallback(object: MqttCallbackExtended {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.w("mqtt-message-arrived", "topic:$topic, message:${message.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.w("mqtt-connection", serverURI?:"Server URI not found")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.w("mqtt-connection", "Disconnected from server")
            }
        })

        connect()
    }

    fun setCallback(mqttCallbackExtended: MqttCallbackExtended) {
        mqttAndroidClient.setCallback(mqttCallbackExtended)
    }

    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = username
        mqttConnectOptions.password = password.toCharArray()

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("mqtt-connection", "Could not connect to server with uri: $serverUri, message: ${exception.toString()}")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.w("mqtt-topic-subscription", "Subscribed to topic: $subscriptionTopic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w(
                        "mqtt-topic-subscription",
                        "Could not subscribe to topic: $subscriptionTopic"
                    )
                }

            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun sendData(topic: String, data: String) {
        val message = MqttMessage()
        message.id = System.currentTimeMillis().toInt()
        message.qos = 0
        message.isRetained = true
        val b = data.toByteArray()
        message.payload = b
        try {
            Log.d("MqttService", "Publishing data=$data to topic=$topic")
            mqttAndroidClient.publish(topic, message)
        } catch (ex: MqttException) {
            Log.d("MqttService", "Could not publish data to topic=$topic, data=$data")
        }
    }
}