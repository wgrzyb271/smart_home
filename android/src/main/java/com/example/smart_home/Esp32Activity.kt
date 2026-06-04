package com.example.smart_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_home.databinding.ActivityEsp32Binding
import okhttp3.*
import java.io.IOException

class Esp32Activity : AppCompatActivity() {
    private lateinit var binding: ActivityEsp32Binding
    private val client = OkHttpClient()
    // Replace with your Flask server IP and port (e.g., 192.168.1.101:5000)
    private val serverUrl = "http://192.168.1.101:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsp32Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleLed.setOnCheckedChangeListener { _, isChecked ->
            val endpoint = if (isChecked) "/led_on" else "/led_off"
            sendRequest(serverUrl + endpoint)
        }

        binding.btnBackFromEsp32.setOnClickListener {
            finish()
        }
    }

    private fun sendRequest(url: String) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }
}