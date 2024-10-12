package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.SelectLoginActivity
import com.example.sos.token.TokenManager

class UserMainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    val logoutManager = LogoutManager(this, tokenManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        tokenManager = TokenManager(this)
        setupButtons()
    }

    private fun setupButtons() {
        val selectUserMapButton = findViewById<ImageButton>(R.id.user_map_button)
        selectUserMapButton.setOnClickListener {
            val intent = Intent(this, UserMapActivity::class.java)
            startActivity(intent)
            finish()
        }

        val selectUserReceptionButton = findViewById<ImageButton>(R.id.user_reception_button)
        selectUserReceptionButton.setOnClickListener {
            val intent = Intent(this, UserReceptionActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userLogoutButton: Button = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {
            logoutManager.logout()
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


