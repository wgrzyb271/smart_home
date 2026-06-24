package com.example.smart_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_home.databinding.ActivitySensorsBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SensorsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySensorsBinding
    private val client = OkHttpClient()
    // Replace with your ESP32 or Flask server URL
//    private val sensorUrl = "http://192.168.0.248:5001/sensors"
    private val sensorUrl = "http://10.0.2.2:5001/sensors"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchSensorData()

        binding.btnRefresh.setOnClickListener {
            fetchSensorData()
        }

        binding.btnBackFromSensors.setOnClickListener {
            finish()
        }
    }

    private fun fetchSensorData() {
        val request = Request.Builder().url(sensorUrl).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        val temp = json.optString("temperature", "--")
                        val humidity = json.optString("humidity", "--")

                        runOnUiThread {
                            binding.tvTemperature.text = getString(R.string.temperature_format, temp)
                            binding.tvHumidity.text = getString(R.string.humidity_format, humidity)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        response.close()
                    }
                }
            }
        })
    }
}
