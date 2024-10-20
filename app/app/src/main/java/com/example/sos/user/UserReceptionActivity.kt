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
import com.example.sos.retrofit.ReceptionInfoResponse
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

        // 뷰 초기화
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val hospitalNameTextView: TextView = findViewById(R.id.hospital_name_textview)
        val hospitalLocationTextView: TextView = findViewById(R.id.hospital_location_textview)
        val hospitalPhoneTextView: TextView = findViewById(R.id.hospital_phone_textview)
        val hospitalCommentTextView: TextView = findViewById(R.id.hospital_comment_textview)

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

        // 병원 정보 조회 버튼 클릭 리스너
        getHospitalInfoButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString()
            if (receptionId.isNotEmpty()) {
                getReceptionInfo(
                    receptionId,
                    hospitalNameTextView,
                    hospitalLocationTextView,
                    hospitalPhoneTextView,
                    hospitalCommentTextView
                )
            }
        }
    }

    private fun getReceptionInfo(
        receptionId: String,
        hospitalNameTextView: TextView,
        hospitalLocationTextView: TextView,
        hospitalPhoneTextView: TextView,
        hospitalCommentTextView: TextView
    ) {
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.getReceptionInfo("Bearer $it", receptionId).enqueue(object :
                Callback<ReceptionInfoResponse> {
                override fun onResponse(
                    call: Call<ReceptionInfoResponse>,
                    response: Response<ReceptionInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { receptionInfoResponse ->
                            val hospital = receptionInfoResponse.data.hospital
                            val comments = receptionInfoResponse.data.comments

                            // 병원 정보 설정
                            hospitalNameTextView.text = "Hospital Name: ${hospital.name}"
                            hospitalLocationTextView.text = "Location: ${hospital.location.longitude}, ${hospital.location.latitude}"
                            hospitalPhoneTextView.text = "Phone: ${hospital.telephoneNumber}"

                            // 코멘트가 있는 경우 첫 번째 코멘트를 표시
                            val comment = if (comments.isNotEmpty()) comments[0].content else "No comments"
                            hospitalCommentTextView.text = "Comment: $comment"
                        }
                    } else {
                        Log.e("Reception", "Failed to fetch hospital info: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ReceptionInfoResponse>, t: Throwable) {
                    Log.e("Reception", "Error: ${t.message}")
                }
            })
        }
    }

    // 네비게이션 아이템 선택 이벤트 처리
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_map -> {
                val intent = Intent(this, UserMapActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_reception -> {
                // 현재 화면이므로 별도의 액션 없음
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
