package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.R

class ParamedicDetailActivity : AppCompatActivity() {
    private lateinit var paramedicIdTextView: TextView
    private lateinit var paramedicNameTextView: TextView
    private lateinit var paramedicPhoneTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_paramedic_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TextView 초기화
        paramedicIdTextView = findViewById(R.id.paramedic_id_text_view)
        paramedicNameTextView = findViewById(R.id.paramedic_name_text_view)
        paramedicPhoneTextView = findViewById(R.id.paramedic_phone_text_view)

        // Intent로부터 넘어온 데이터 변수에 저장
        val paramedicId = intent.getStringExtra("id")
        val paramedicName = intent.getStringExtra("name")
        val paramedicPhoneNumber = intent.getStringExtra("phoneNumber")

        // TextView에 데이터 설정 (화면에 보일 값 넘기기)
        paramedicIdTextView.text = paramedicId
        paramedicNameTextView.text = paramedicName
        paramedicPhoneTextView.text = paramedicPhoneNumber

        // 수정 버튼 클릭 리스너 설정
        val paramedicModifyButton = findViewById<Button>(R.id.paramedic_modify_button)
        paramedicModifyButton.setOnClickListener {
            val intent = Intent(this, AmbulanceParamedicModifyActivity::class.java)
            intent.putExtra("id", paramedicId) // id 전달
            intent.putExtra("name", paramedicName) // 이름 전달
            intent.putExtra("phoneNumber", paramedicPhoneNumber) // 전화번호 전달
            startActivity(intent)
        }
    }
}
