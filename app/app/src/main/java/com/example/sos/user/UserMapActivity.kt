package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserMapActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var mapView: MapView
    private lateinit var authService: AuthService
    private var kakaoMap: KakaoMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        mapView = findViewById(R.id.map_view)
        initializeMapView()

        // BottomNavigationView 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> true // 현재 화면이므로 아무 동작도 하지 않음
                R.id.nav_reception -> {
                    val intent = Intent(this, UserReceptionActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        fetchHospitalData()
    }

    private fun initializeMapView() {
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("UserMapActivity", "Map destroyed")
            }

            override fun onMapError(exception: Exception?) {
                Log.e("UserMapActivity", "Map error: ${exception?.message}")
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                val yuCenter = LatLng.from(35.8264595, 128.754132) // 영남대 좌표
                kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(yuCenter, 15)) // 지도 중심 설정
            }
        })
    }

    private fun fetchHospitalData() {
        val accessToken = tokenManager.getAccessToken() ?: ""
        val token = "Bearer $accessToken"
        val categories = listOf("산부인과", "정형외과", "흉부외과", "화상외과", "내과")
        val page = 1

        authService.searchHospital(token, categories, page).enqueue(object : Callback<SearchHospitalResponse> {
            override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.hospitals?.let { hospitals ->
                        addLabelsToMap(hospitals)
                    }
                } else {
                    Log.e("UserMapActivity", "Response Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SearchHospitalResponse>, t: Throwable) {
                Log.e("UserMapActivity", "Request Failure: ${t.message}")
            }
        })
    }

    private fun addLabelsToMap(hospitals: List<HospitalRes>) {
        val labelManager: LabelManager? = kakaoMap?.labelManager

        hospitals.forEach { hospital ->
            val latitude = hospital.location.latitude.toDouble()
            val longitude = hospital.location.longitude.toDouble()
            val position = LatLng.from(latitude, longitude)

            val labelStyles = LabelStyles.from(
                "hospitalStyle",
                LabelStyle.from(R.drawable.user_map).setZoomLevel(8),
                LabelStyle.from(R.drawable.user_map).setZoomLevel(11)
                    .setTextStyles(32, android.graphics.Color.BLACK, 1, android.graphics.Color.GRAY)
            )

            val labelText = LabelTextBuilder()
                .setTexts(hospital.name, hospital.telephoneNumber)

            val labelOptions = LabelOptions.from(position)
                .setStyles(labelStyles)
                .setTexts(labelText)

            labelManager?.layer?.addLabel(labelOptions)
        }
    }
}
