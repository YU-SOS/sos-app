package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.ParamedicsRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
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
        fetchParamedics(ambulanceId)
    }

    private fun fetchParamedics(ambulanceId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getParamedics("Bearer $jwtToken", ambulanceId).enqueue(object : Callback<List<ParamedicsRes>> {
                override fun onResponse(call: Call<List<ParamedicsRes>>, response: Response<List<ParamedicsRes>>) {
                    if (response.isSuccessful) {
                        val paramedicsList = response.body()
                        if (!paramedicsList.isNullOrEmpty()) {
                            setupRecyclerView(paramedicsList)
                        } else {
                            showToast("구급대원이 없습니다.")
                        }
                    } else {
                        showToast("구급대원 로딩 실패")
                    }
                }

                override fun onFailure(call: Call<List<ParamedicsRes>>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        } else {
            showToast("토큰 오류")
        }
    }

    private fun setupRecyclerView(paramedicsList: List<ParamedicsRes>) {
        val adapter = ParamedicsAdapter(paramedicsList) { selectedParamedic ->
            val intent = Intent(this, DetailParamedicActivity::class.java).apply {
                putExtra("paramedicId", selectedParamedic.id)
                putExtra("paramedicName", selectedParamedic.name)
                putExtra("paramedicPhone", selectedParamedic.phoneNumber)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
