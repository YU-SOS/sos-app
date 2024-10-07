package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.ambulance.LoginAmbulanceActivity
import com.example.sos.token.TokenManager
import com.example.sos.user.UserLoginActivity

class SelectLoginActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_login)
        tokenManager = TokenManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val selectAmbulanceButton = findViewById<ImageButton>(R.id.select_ambulance_button)
        selectAmbulanceButton.setOnClickListener {
            val intent = Intent(this, LoginAmbulanceActivity::class.java)
            startActivity(intent)
        }

        val selectUserButton = findViewById<ImageButton>(R.id.select_user_button)
        selectUserButton.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }
    }
}