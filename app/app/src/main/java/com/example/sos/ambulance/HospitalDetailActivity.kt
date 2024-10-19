package com.example.sos.ambulance

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R

class HospitalDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_detail)

        // Intent에서 데이터 가져오기
        val hospitalId = intent.getStringExtra("HOSPITAL_ID") ?: return
        val hospitalName = intent.getStringExtra("HOSPITAL_NAME") ?: return
        val hospitalAddress = intent.getStringExtra("HOSPITAL_ADDRESS") ?: return
        val hospitalTelephone = intent.getStringExtra("HOSPITAL_TELEPHONE") ?: return
        val hospitalImageUrl = intent.getStringExtra("HOSPITAL_IMAGE_URL") ?: return

        // TextView에 병원 상세 정보 설정
        findViewById<TextView>(R.id.hospitalNameTextView).text = hospitalName
        findViewById<TextView>(R.id.hospitalAddressTextView).text = hospitalAddress
        findViewById<TextView>(R.id.hospitalTelephoneTextView).text = hospitalTelephone
        // 이미지가 필요하다면 Glide 또는 다른 이미지 로딩 라이브러리로 로드할 수 있습니다.
    }
}
