package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.SelectLoginActivity
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalDetailResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserReceptionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reception)

        tokenManager = TokenManager(this)

        apiService = RetrofitClientInstance.getApiService(tokenManager)

        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)

        val hospitalNameTextView: TextView = findViewById(R.id.hospital_name_textview)
        val hospitalLocationTextView: TextView = findViewById(R.id.hospital_location_textview)
        val hospitalPhoneTextView: TextView = findViewById(R.id.hospital_phone_textview)
        val hospitalCommentTextView: TextView = findViewById(R.id.hospital_comment_textview)

        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {

            val logoutManager = LogoutManager(this, tokenManager)
            logoutManager.logout()

            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 버튼 클릭 시 서버 요청
        getHospitalInfoButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString()

            if (receptionId.isNotEmpty()) {
                getHospitalInfo(receptionId, hospitalNameTextView, hospitalLocationTextView, hospitalPhoneTextView, hospitalCommentTextView)
            }
        }
    }

    private fun getHospitalInfo(
        receptionId: String,
        hospitalNameTextView: TextView,
        hospitalLocationTextView: TextView,
        hospitalPhoneTextView: TextView,
        hospitalCommentTextView: TextView
    ) {
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.getHospitalDetails("Bearer $it", receptionId).enqueue(object :
                Callback<HospitalDetailResponse> {
                override fun onResponse(call: Call<HospitalDetailResponse>, response: Response<HospitalDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            // 병원 이름, 위치, 전화번호, 코멘트 설정
                            hospitalNameTextView.text = "Hospital Name: ${hospitalResponse.name}"
                            hospitalLocationTextView.text = "Location: ${hospitalResponse.location.longitude}, ${hospitalResponse.location.latitude}"
                            hospitalPhoneTextView.text = "Phone: ${hospitalResponse.telephoneNumber}"
                            hospitalCommentTextView.text = "Comment: ${hospitalResponse.message}"
                        }
                    } else {
                        Log.e("Reception", "Failed to fetch hospital info: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HospitalDetailResponse>, t: Throwable) {
                    Log.e("Reception", "Error: ${t.message}")
                }
            })
        }

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
                // Navigate to Map Activity
                val intent = Intent(this, UserMapActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_reception -> {
                // Navigate to Reception Activity
                val intent = Intent(this, UserReceptionActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
