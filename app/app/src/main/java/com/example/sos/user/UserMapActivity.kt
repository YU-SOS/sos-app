package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.sos.HospitalRes
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.SelectLoginActivity
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserMapActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tokenManager: TokenManager
    private lateinit var hospitalContainer: LinearLayout
    private lateinit var pediatricsCheckBox: CheckBox
    private lateinit var orthopedicsCheckBox: CheckBox
    private lateinit var searchButton: Button
    private lateinit var logoutManager: LogoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        tokenManager = TokenManager(this)

        hospitalContainer = findViewById(R.id.hospitalContainer)
        pediatricsCheckBox = findViewById(R.id.checkBoxPediatrics)
        orthopedicsCheckBox = findViewById(R.id.checkBoxOrthopedics)
        searchButton = findViewById(R.id.search_hospital_button)
        logoutManager = LogoutManager(this, tokenManager)

        searchButton.setOnClickListener {
            searchHospitalDetails()
        }
        val userLogoutButton: Button = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {

            logoutManager.logout()

            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 네비게이션 바 설정
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_map -> {
                Log.d("UserMapActivity", "Map selected")
                // 현재 화면이므로 별도 액션 필요 없음
            }
            R.id.nav_reception -> {
                Log.d("UserMapActivity", "Reception selected")
                // 다른 액티비티로 이동
                val intent = Intent(this, UserReceptionActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun searchHospitalDetails() {
        val accessToken = tokenManager.getAccessToken()
        val selectedCategories = mutableListOf<String>()

        if (pediatricsCheckBox.isChecked) {
            selectedCategories.add("소아과")
        }
        if (orthopedicsCheckBox.isChecked) {
            selectedCategories.add("정형외과")
        }

        if (selectedCategories.isEmpty()) {
            Log.d("UserMapActivity", "카테고리를 선택해주세요.")
            return
        }

        accessToken?.let {
            val apiService = RetrofitClientInstance.getApiService(tokenManager)
            apiService.searchHospital("Bearer $it", selectedCategories, 0).enqueue(object : Callback<SearchHospitalResponse> {
                override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            displayHospitals(hospitalResponse.data.hospitals)
                        }
                    } else {
                        Log.e("UserMapActivity", "Request failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SearchHospitalResponse>, t: Throwable) {
                    Log.e("UserMapActivity", "Request failed: ${t.message}")
                }
            })
        }
    }

    private fun displayHospitals(hospitals: List<HospitalRes>) {
        hospitalContainer.removeAllViews()

        for (hospital in hospitals) {
            val hospitalTextView = TextView(this).apply {
                text = """
                    이름: ${hospital.name}
                    전화번호: ${hospital.telephoneNumber}
                """.trimIndent()
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            hospitalContainer.addView(hospitalTextView)
        }
    }
}
