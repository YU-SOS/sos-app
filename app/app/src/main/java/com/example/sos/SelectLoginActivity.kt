package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.ambulance.LoginAmbulanceActivity
import com.example.sos.user.UserLoginActivity

class SelectLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 구급대 버튼을 눌렀을 때 로그인 구급대 액티비티로 이동
        val selectUserButton = findViewById<ImageButton>(R.id.select_ambulance_button)
        selectUserButton.setOnClickListener {
            val intent = Intent(this, LoginAmbulanceActivity::class.java)
            startActivity(intent)
            // finish() 이건 돌아갈 필요가 없으면 finish 있으면 대기.
        }

        // 사용자 버튼을 눌렀을 때 로그인 사용자 액티비티로 이동
        val selectAmbulanceButton = findViewById<ImageButton>(R.id.select_user_button)
        selectAmbulanceButton.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
            // finish() 이건 돌아갈 필요가 없으면 finish 있으면 대기.
        }
    }
}
