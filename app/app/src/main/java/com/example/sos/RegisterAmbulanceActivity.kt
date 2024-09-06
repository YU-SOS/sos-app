package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterAmbulanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_ambulance)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val selectUserButton = findViewById<Button>(R.id.buttonSubmit)
        selectUserButton.setOnClickListener {
            if (checkRegisterStatus()) { // 회원 가입 성공 시
                val intent = Intent(this, MainActivity::class.java) // main 페이지로 이동 (현재 main페이지에 로그인 관련 로직이 없으므로 로그인 선택 창으로 이동될거임)
                startActivity(intent)
                finish() // 회원가입 액티비티는 종료 (추후 킬 이유 없음)
            }
        }
    }
    private fun checkRegisterStatus(): Boolean { // 추후 회원가입 성공여부를 체크 해줄 함수 (만들어야됨)
        return true
        // 아마 아이디 조건, 비밀번호 조건, 주소 등등등을 확인하는 로직일거임.
    }
}