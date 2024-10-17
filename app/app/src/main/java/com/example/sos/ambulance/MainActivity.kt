package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.token.TokenManager

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var tokenManager: TokenManager // TokenManager 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)  // TokenManager 초기화
        logoutButton = findViewById(R.id.logout)
        val logoutManager = LogoutManager(this, tokenManager)

        val searchHospitalButton = findViewById<Button>(R.id.search_hospital_button)
        searchHospitalButton.setOnClickListener {
            val intent = Intent(this, AmbulanceSearchHospitalActivity::class.java)
            startActivity(intent)
            //finish()
        }

        val addPatientButton = findViewById<Button>(R.id.add_patient_button)
        addPatientButton.setOnClickListener {
            val intent = Intent(this, AmbulanceAddPatientActivity::class.java)
            startActivity(intent)
            //finish()
        }

        val addMemberButton = findViewById<Button>(R.id.add_member_button)
        addMemberButton.setOnClickListener{
            val intent = Intent(this, AmbulanceAddMemberActivity::class.java)
            startActivity(intent)
            //finish()
        }

        val loadMemberButton = findViewById<Button>(R.id.load_member_button)
        loadMemberButton.setOnClickListener{
            val intent = Intent(this, AmbulanceLoadMemberActivity::class.java)
            startActivity(intent)
            //finish() 메인 화면은 그대로 유지되는 것이 맞지 않을까요?
        }

        logoutButton.setOnClickListener {
            logoutManager.logout()
        }
    }
}
