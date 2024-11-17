package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_main)  // XML 레이아웃은 activity_main.xml로 설정합니다.

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    val intent = Intent(this, UserMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_reception -> {
                    val intent = Intent(this, UserReceptionActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
