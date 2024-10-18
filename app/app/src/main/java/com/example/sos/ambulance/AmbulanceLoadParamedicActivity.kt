package com.example.sos.ambulance

import ParamedicsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.ParamedicsRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceLoadParamedicActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService
    private lateinit var paramedicsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_load_paramedic)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        paramedicsListView = findViewById(R.id.list_view_paramedics)

        val ambulanceId = tokenManager.getTokenId() // 토큰에서 ambulanceId 가져오기
        if (ambulanceId != null) {
            loadParamedics(ambulanceId)
        } else {
            Log.e("ParamedicsLoad", "Error: Ambulance ID is null")
        }

        // 리스트 아이템 클릭 리스너 추가
        paramedicsListView.setOnItemClickListener { parent, view, position, id ->
            val selectedParamedic = (paramedicsListView.adapter as ParamedicsAdapter).getItem(position)
            selectedParamedic?.let {
                // 선택한 항목의 정보를 넘기기 위한 Intent 생성
                val intent = Intent(this, ParamedicDetailActivity::class.java)
                intent.putExtra("id", it.id) // ID 전달
                intent.putExtra("name", it.name) // 이름 전달
                intent.putExtra("phoneNumber", it.phoneNumber) // 전화번호 전달

                startActivity(intent) // 새로운 액티비티로 이동
            }
        }
    }

    private fun loadParamedics(ambulanceId: String) {
        val authorization = "Bearer ${tokenManager.getAccessToken()}"

        authService.getParamedics(authorization, ambulanceId).enqueue(object : Callback<List<ParamedicsRes>> {
            override fun onResponse(
                call: Call<List<ParamedicsRes>>,
                response: Response<List<ParamedicsRes>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { paramedicsList ->
                        displayParamedics(paramedicsList)
                    } ?: run {
                        Log.e("ParamedicsLoad", "Response is empty")
                    }
                } else {
                    Log.e("ParamedicsLoad", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ParamedicsRes>>, t: Throwable) {
                Log.e("ParamedicsLoad", "Failure: ${t.message}")
            }
        })
    }

    private fun displayParamedics(paramedicsList: List<ParamedicsRes>) {
        val adapter = ParamedicsAdapter(this, paramedicsList)
        paramedicsListView.adapter = adapter
    }
}
