package com.example.sos.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
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
                    true
                }
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

                fetchHospitalData()

                kakaoMap?.setOnMapClickListener { _, latLng, _, _ ->
                    val clickedHospital = findClosestHospital(latLng)
                    clickedHospital?.let { hospital ->
                        showHospitalDetails(hospital)
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

            val labelStyles = LabelStyles.from(
                "hospitalStyle",
                LabelStyle.from(R.drawable.hospital_point).setZoomLevel(8),
                LabelStyle.from(R.drawable.hospital_point).setZoomLevel(11)
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_hospital_detail_button, null)

        dialog.setContentView(dialogView)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.window?.setGravity(android.view.Gravity.CENTER)

        val hospitalName = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_name)
        val hospitalImage = dialogView.findViewById<ImageView>(R.id.bottom_sheet_hospital_image)
        val hospitalAddress = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_address)
        val hospitalPhone = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_phone)
        val hospitalCategories = dialogView.findViewById<TextView>(R.id.bottom_sheet_hospital_categories)

        hospitalName.text = hospital.name
        hospitalAddress.text = "주소: ${hospital.address}"
        hospitalPhone.text = "전화번호: ${hospital.telephoneNumber}"

        val categoryNames = hospital.categories.joinToString(", ") { it.name }
        hospitalCategories.text = "카테고리: ${categoryNames}"
        Glide.with(this).load(hospital.imageUrl).into(hospitalImage)
        hospitalName.textSize = 42f
        hospitalAddress.textSize = 18f
        hospitalPhone.textSize = 18f
        hospitalCategories.textSize = 18f

        dialog.show()
    }

}
