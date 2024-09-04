package com.example.sos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!checkLoginStatus()) {
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent) // 로그인이 안되어 있을 경우 SelectLoginActivity를 띄움.
            finish()
        }
    }
    private fun checkLoginStatus(): Boolean { // 추후 로그인 상태를 체크 해줄 함수
        return false
    }
}