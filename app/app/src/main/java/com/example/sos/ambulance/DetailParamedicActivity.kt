package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicDeleteResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var paramedicId: String
    private lateinit var ambulanceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_paramedic)

        authService = RetrofitClientInstance.getApiService(TokenManager(this))
        paramedicId = intent.getStringExtra("paramedicId") ?: ""
        ambulanceId = tokenManager.getTokenId()

        findViewById<TextView>(R.id.paramedic_name).text = intent.getStringExtra("paramedicName")
        findViewById<TextView>(R.id.paramedic_phone).text = intent.getStringExtra("paramedicPhone")

        findViewById<Button>(R.id.btn_paramedic_delete).setOnClickListener {
            deleteParamedic()
        }

        findViewById<Button>(R.id.btn_paramedic_modify).setOnClickListener {
            val intent = Intent(this, ModifyParamedicActivity::class.java).apply {
                putExtra("paramedicId", paramedicId)
                putExtra("paramedicName", intent.getStringExtra("paramedicName"))
                putExtra("paramedicPhone", intent.getStringExtra("paramedicPhone"))
            }
            startActivity(intent)
        }
    }

    private fun deleteParamedic() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.deleteParamedic("Bearer $jwtToken", ambulanceId, paramedicId).enqueue(object : Callback<ParamedicDeleteResponse> {
                override fun onResponse(call: Call<ParamedicDeleteResponse>, response: Response<ParamedicDeleteResponse>) {
                    if (response.isSuccessful && response.body()?.status == 200) {
                        Toast.makeText(this@DetailParamedicActivity, "삭제 성공", Toast.LENGTH_SHORT).show()
                        finish() // 구급대원 페이지로 돌아가기
                    } else {
                        showToast("삭제 실패")
                    }
                }
                override fun onFailure(call: Call<ParamedicDeleteResponse>, t: Throwable) {
                    showToast("삭제 오류: ${t.message}")
                }
            })

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
