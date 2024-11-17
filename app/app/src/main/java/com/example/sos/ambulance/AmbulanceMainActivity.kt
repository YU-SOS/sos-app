package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.token.TokenManager

class AmbulanceMainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var tokenManager: TokenManager // TokenManager 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_main)

        tokenManager = TokenManager(this)  // TokenManager 초기화
        logoutButton = findViewById(R.id.logout)
        val logoutManager = LogoutManager(this, tokenManager)

        val addPatientButton = findViewById<ImageButton>(R.id.add)
        addPatientButton.setOnClickListener {
            val intent = Intent(this, AddPatientActivity::class.java)
            startActivity(intent)
            //finish()
        }

        val loadAmbulanceButton = findViewById<ImageButton>(R.id.load)
        loadAmbulanceButton.setOnClickListener{
            val intent = Intent(this , LoadAmbulanceActivity::class.java)
            startActivity(intent)
        }

        val loadHospitalButton = findViewById<ImageButton>(R.id.search)
        loadHospitalButton.setOnClickListener{
            val intent = Intent(this, LoadHospitalActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            logoutManager.logout()
        }
    }
}
