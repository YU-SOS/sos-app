package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.res.ParamedicsRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicsResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 구급대원들을 보여주는 Activity
class LoadParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_paramedic)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_view_paramedics)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val ambulanceId = tokenManager.getTokenId()
        if (!ambulanceId.isNullOrEmpty()) {
            fetchParamedics(ambulanceId)
        } else {
            showToast("구급대 ID를 찾을 수 없습니다. 다시 로그인하세요.")
        }
    }

    private fun fetchParamedics(ambulanceId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        Log.d("LoadParamedicActivity", "Fetching paramedics for ambulanceId: $ambulanceId")
        authService.getParamedics("Bearer $jwtToken", ambulanceId)
            .enqueue(object : Callback<ParamedicsResponse> {
                override fun onResponse(call: Call<ParamedicsResponse>, response: Response<ParamedicsResponse>) {
                    if (response.isSuccessful) {
                        val paramedicsList = response.body()?.data // 변경된 구조에 맞게 수정
                        Log.d("LoadParamedicActivity", "API Response: $paramedicsList")
                        if (!paramedicsList.isNullOrEmpty()) {
                            setupRecyclerView(paramedicsList, ambulanceId)
                        } else {
                            Log.d("LoadParamedicActivity", "Paramedics list is empty")
                            showToast("구급대원이 없습니다.")
                        }
                    } else {
                        Log.e("LoadParamedicActivity", "API Response Error: ${response.message()}")
                        showToast("구급대원 로딩 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ParamedicsResponse>, t: Throwable) {
                    Log.e("LoadParamedicActivity", "API Call Failed: ${t.message}")
                    showToast("서버 연결 실패: ${t.message}")
                }
            })
    }

    private fun setupRecyclerView(paramedicsList: List<ParamedicsRes>, ambulanceId: String) {
        if (paramedicsList.isEmpty()) {
            showToast("표시할 구급대원이 없습니다.")
            return
        }

        val adapter = ParamedicsAdapter(paramedicsList) { selectedParamedic ->
            val intent = Intent(this, DetailParamedicActivity::class.java).apply {
                putExtra("paramedicId", selectedParamedic.id)
                putExtra("paramedicName", selectedParamedic.name)
                putExtra("paramedicPhone", selectedParamedic.phoneNumber)
                putExtra("ambulanceId", ambulanceId) // 구급대 ID 추가
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
