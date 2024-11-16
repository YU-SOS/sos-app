package com.example.sos.ambulance

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.sos.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalDetailResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailHospitalActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var hospitalImage: ImageView
    private lateinit var hospitalName: TextView
    private lateinit var hospitalPhone: TextView
    private lateinit var hospitalAddress: TextView
    private lateinit var hospitalCategories: TextView
    private lateinit var hospitalStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_hospital)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authService = RetrofitClientInstance.getApiService(TokenManager(this))
        hospitalImage = findViewById(R.id.hospital_image)
        hospitalName = findViewById(R.id.hospital_name)
        hospitalPhone = findViewById(R.id.hospital_phone)
        hospitalAddress = findViewById(R.id.hospital_address)
        hospitalCategories = findViewById(R.id.hospital_categories)
        hospitalStatus = findViewById(R.id.hospital_status)

        val hospitalId = intent.getStringExtra("hospitalId")
        if (hospitalId != null) {
            fetchHospitalDetails(hospitalId)
        } else {
            Toast.makeText(this, "병원 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchHospitalDetails(hospitalId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getHospitalDetails("Bearer $jwtToken", hospitalId).enqueue(object : Callback<HospitalDetailResponse> {
                override fun onResponse(call: Call<HospitalDetailResponse>, response: Response<HospitalDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.data?.let { hospital ->
                            displayHospitalDetails(hospital)
                        }
                    } else {
                        Toast.makeText(this@DetailHospitalActivity, "병원 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<HospitalDetailResponse>, t: Throwable) {
                    Toast.makeText(this@DetailHospitalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "토큰을 찾을 수 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun displayHospitalDetails(hospital: HospitalRes) {
        hospitalName.text = hospital.name
        hospitalPhone.text = "전화번호: ${hospital.telephoneNumber}"
        hospitalAddress.text = "주소: ${hospital.address}"
        hospitalCategories.text = "카테고리: ${hospital.categories.joinToString { it.name }}"

        val statusText = if (hospital.emergencyRoomStatus) "수용 가능" else "수용 불가"
        hospitalStatus.text = "응급실 상태: $statusText"

        Glide.with(this)
            .load(hospital.imageUrl)
            .into(hospitalImage)
    }
}