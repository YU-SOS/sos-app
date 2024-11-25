package com.example.sos.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.res.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalLoadResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private val labelMap = mutableMapOf<LatLng, HospitalRes>() // 병원 좌표와 데이터를 매핑하는 Map

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        mapView = findViewById(R.id.map_view)
        initializeMapView()

        // BottomNavigationView 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_map

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    // 이미 현재 화면이므로 아무 작업도 하지 않음
                    true
                }
                R.id.nav_reception -> {
                    val intent = Intent(this, UserReceptionActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
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

                // 지도 클릭 이벤트
                kakaoMap?.setOnMapClickListener { _, latLng, _, _ ->
                    val clickedHospital = findClosestHospital(latLng)
                    clickedHospital?.let { hospital ->
                        showHospitalDetails(hospital) // 병원 정보 표시
                    }
                }
            }
        })
    }

    private fun fetchHospitalData() {
        val accessToken = tokenManager.getAccessToken() ?: ""
        val token = "Bearer $accessToken"

        authService.getHospitalList(token, null, 0).enqueue(object :
            Callback<HospitalLoadResponse<HospitalRes>> {
            override fun onResponse(
                call: Call<HospitalLoadResponse<HospitalRes>>,
                response: Response<HospitalLoadResponse<HospitalRes>>
            ) {
                if (response.isSuccessful) {
                    val hospitals = response.body()?.data?.content
                    if (!hospitals.isNullOrEmpty()) {
                        addLabelsToMap(hospitals)
                    }
                }
            }

            override fun onFailure(call: Call<HospitalLoadResponse<HospitalRes>>, t: Throwable) {
                Log.e("UserMapActivity", "Error fetching hospital data: ${t.message}")
            }
        })
    }

    private fun addLabelsToMap(hospitals: List<HospitalRes>) {
        val labelManager = kakaoMap?.labelManager ?: return

        hospitals.forEach { hospital ->
            val latitude = hospital.location.latitude.toDoubleOrNull() ?: return@forEach
            val longitude = hospital.location.longitude.toDoubleOrNull() ?: return@forEach
            val position = LatLng.from(latitude, longitude)

            // 라벨 텍스트 및 스타일 설정
            val labelStyles = LabelStyles.from(
                "hospitalStyle",
                LabelStyle.from(R.drawable.user_map).setZoomLevel(8),
                LabelStyle.from(R.drawable.user_map).setZoomLevel(11)
                    .setTextStyles(32, android.graphics.Color.BLACK, 1, android.graphics.Color.GRAY)
            )

            val labelText = LabelTextBuilder()
                .setTexts(hospital.name ?: "병원 이름 없음")

            val labelOptions = LabelOptions.from(position)
                .setStyles(labelStyles)
                .setTexts(labelText)

            try {
                labelManager.layer?.addLabel(labelOptions)
                labelMap[position] = hospital
            } catch (e: Exception) {
                Log.e("UserMapActivity", "Error adding label: ${e.message}", e)
            }
        }
    }

    private fun findClosestHospital(latLng: LatLng): HospitalRes? {
        var closestDistance = Double.MAX_VALUE
        var closestHospital: HospitalRes? = null

        labelMap.forEach { (position, hospital) ->
            val distance = calculateDistance(latLng, position)
            if (distance < closestDistance) {
                closestDistance = distance
                closestHospital = hospital
            }
        }
        return closestHospital
    }

    private fun calculateDistance(pos1: LatLng, pos2: LatLng): Double {
        val dx = pos1.latitude - pos2.latitude
        val dy = pos1.longitude - pos2.longitude
        return Math.sqrt(dx * dx + dy * dy)
    }

    private fun showHospitalDetails(hospital: HospitalRes) {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_button_hospital_detail, null)

        // Dialog에 레이아웃 설정
        dialog.setContentView(dialogView)

        // Dialog 크기 조정 (화면의 중앙에 표시)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Dialog 배경 설정 (둥근 흰색 배경)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        // Dialog 화면 중앙 정렬
        dialog.window?.setGravity(android.view.Gravity.CENTER)

        // 레이아웃에서 UI 요소 초기화
        val hospitalName = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_name)
        val hospitalImage = dialogView.findViewById<ImageView>(R.id.bottom_sheet_hospital_image)
        val hospitalAddress = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_address)
        val hospitalPhone = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_phone)
        val hospitalCategories = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_categories)

        // 병원 정보 설정
        hospitalName.text = hospital.name
        hospitalAddress.text = "주소: ${hospital.address}"
        hospitalPhone.text = "전화번호: ${hospital.telephoneNumber}"

        // 병원 카테고리 설정
        val categoryNames = hospital.categories.joinToString(", ") { it.name }
        hospitalCategories.text = "카테고리: ${categoryNames}"

        // 병원 이미지 로드
        Glide.with(this).load(hospital.imageUrl).into(hospitalImage)

        // 텍스트 크기 조정
        hospitalName.textSize = 42f // 병원 이름 텍스트 크기
        hospitalAddress.textSize = 18f // 병원 주소 텍스트 크기
        hospitalPhone.textSize = 18f // 병원 전화번호 텍스트 크기
        hospitalCategories.textSize = 18f // 병원 카테고리 텍스트 크기

        // 다이얼로그 표시
        dialog.show()
    }

}
