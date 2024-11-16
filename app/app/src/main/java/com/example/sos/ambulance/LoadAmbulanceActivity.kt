package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.ParamedicsRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 구급대 정보를 띄우는 Activity / 선탑 구급대원을 선택할 수 있음.
class LoadAmbulanceActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var paramedicSpinner: Spinner
    private var selectedParamedic: ParamedicsRes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_ambulance)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)
        paramedicSpinner = findViewById(R.id.spinner_paramedics)

        val ambulanceId = tokenManager.getTokenId()
        fetchParamedics(ambulanceId)

        val loadParamedicButton = findViewById<Button>(R.id.btn_load_paramedic)
        loadParamedicButton.setOnClickListener{
            val intent = Intent(this, LoadParamedicActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchParamedics(ambulanceId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getParamedics("Bearer $jwtToken", ambulanceId).enqueue(object : Callback<List<ParamedicsRes>> {
                override fun onResponse(call: Call<List<ParamedicsRes>>, response: Response<List<ParamedicsRes>>) {
                    if (response.isSuccessful) {
                        val paramedicsList = response.body()
                        if (!paramedicsList.isNullOrEmpty()) {
                            val paramedicNames = paramedicsList.map { it.name }
                            setupParamedicSpinner(paramedicsList, paramedicNames)
                        } else {
                            // 구급대원이 없는 경우
                            setupParamedicSpinner(emptyList(), listOf("구급대원이 없습니다."))
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

    private fun setupParamedicSpinner(paramedicsList: List<ParamedicsRes>, paramedicNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paramedicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paramedicSpinner.adapter = adapter

        paramedicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedParamedic = paramedicsList[position]
                selectedParamedic?.let {
                    tokenManager.saveSelectedParamedicId(it.id) // 선택된 구급대원의 ID 저장 (JWT처럼)
                    showToast("Selected Paramedic: ${it.name}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedParamedic = null
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
