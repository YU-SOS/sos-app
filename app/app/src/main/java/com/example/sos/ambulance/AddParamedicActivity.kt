package com.example.sos.ambulance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.MemberRequest
import com.example.sos.retrofit.MemberResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_paramedic)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        nameEditText = findViewById(R.id.edit_text_name)
        phoneEditText = findViewById(R.id.edit_text_phone)

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            addParamedic()
        }
    }

    private fun addParamedic() {
        val jwtToken = tokenManager.getAccessToken()
        val ambulanceId = try {
            tokenManager.getTokenId() // TokenManager를 통해 구급대 ID 가져오기
        } catch (e: Exception) {
            showToast("구급대 ID를 가져오는 데 실패했습니다: ${e.message}")
            return
        }

        if (jwtToken != null) {
            val paramedic = MemberRequest(
                name = nameEditText.text.toString(),
                phoneNumber = phoneEditText.text.toString()
            )

            authService.addAmbulanceMember(
                "Bearer $jwtToken",
                ambulanceId,
                paramedic // BODY로 전송할 구급대원 정보
            ).enqueue(object : Callback<MemberResponse> {
                override fun onResponse(call: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful) {
                        showToast("구급대원 추가 성공")
                        finish() // 구급대원 페이지로 돌아가기
                    } else {
                        showToast("구급대원 추가 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MemberResponse>, t: Throwable) {
                    showToast("추가 오류: ${t.message}")
                }
            })
        } else {
            showToast("토큰을 찾을 수 없습니다. 다시 로그인 해주세요.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
