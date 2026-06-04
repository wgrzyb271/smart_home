package com.example.smart_home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.smart_home.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val esp32Ip = "http://192.168.1.101"
    private var lightOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.btnEsp32.setOnClickListener {
            startActivity(Intent(this, Esp32Activity::class.java))
        }

        binding.btnMainLight.setOnClickListener {
            lightOn = !lightOn
            binding.btnMainLight.text = if (lightOn) "Turn light Off" else "Turn light On"
            val endpoint = if (lightOn) "/led_on" else "/led_off"
            sendRequest(esp32Ip + endpoint)
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