    package com.example.sos.user

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import androidx.appcompat.app.AppCompatActivity
    import com.example.sos.res.HospitalRes
    import com.example.sos.R
    import com.example.sos.retrofit.AuthService
    import com.example.sos.retrofit.HospitalLoadResponse
    import com.example.sos.retrofit.RetrofitClientInstance
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

                    // 지도 초기화 완료 후 병원 데이터 요청
                    fetchHospitalData()
                }
            })
        }

        private fun fetchHospitalData() {
            val accessToken = tokenManager.getAccessToken() ?: ""
            val token = "Bearer $accessToken"
            val categories: List<String>? = null
            val page = 0

            authService.getHospitalList(token, categories, page).enqueue(object : Callback<HospitalLoadResponse<HospitalRes>> {
                override fun onResponse(call: Call<HospitalLoadResponse<HospitalRes>>, response: Response<HospitalLoadResponse<HospitalRes>>) {
                    if (response.isSuccessful) {
                        val hospitals = response.body()?.data?.content
                        if (!hospitals.isNullOrEmpty()) {
                            addLabelsToMap(hospitals)
                        } else {
                            Log.e("UserMapActivity", "No hospitals found")
                        }
                    } else {
                        Log.e("UserMapActivity", "Response Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HospitalLoadResponse<HospitalRes>>, t: Throwable) {
                    Log.e("UserMapActivity", "Request Failed: ${t.message}", t)
                }
            })
        }

        private fun addLabelsToMap(hospitals: List<HospitalRes>) {
            if (kakaoMap == null || kakaoMap?.labelManager == null) {
                Log.e("UserMapActivity", "KakaoMap or LabelManager is not initialized.")
                return
            }

            val labelManager = kakaoMap?.labelManager

            hospitals.forEach { hospital ->
                val latitude = hospital.location.latitude.toDoubleOrNull() ?: return@forEach
                val longitude = hospital.location.longitude.toDoubleOrNull() ?: return@forEach
                val position = LatLng.from(latitude, longitude)

                Log.d("UserMapActivity", "Adding label for ${hospital.name} at ($latitude, $longitude)")

                // 수정된 라벨 스타일
                val labelStyles = LabelStyles.from(
                    "hospitalStyle",
                    LabelStyle.from(R.drawable.user_map).setZoomLevel(8), // user_map drawable 사용
                    LabelStyle.from(R.drawable.user_map).setZoomLevel(11)
                        .setTextStyles(32, android.graphics.Color.BLACK, 1, android.graphics.Color.GRAY)
                )

                val labelText = LabelTextBuilder()
                    .setTexts("병원이름:",hospital.name ?: "병원 이름 없음","전화번호: ", hospital.telephoneNumber ?: "전화번호 없음")

                val labelOptions = LabelOptions.from(position)
                    .setStyles(labelStyles)
                    .setTexts(labelText)

                try {
                    labelManager?.layer?.addLabel(labelOptions)
                } catch (e: Exception) {
                    Log.e("UserMapActivity", "Error adding label: ${e.message}", e)
                }
            }
        }


    }
