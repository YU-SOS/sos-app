package com.example.sos.ambulance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionCommentResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceAddCommentActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService

    private lateinit var commentEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_add_comment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize components
        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        commentEditText = findViewById(R.id.commentEditText)
        submitButton = findViewById(R.id.submitCommentButton)

        // Set up button click listener
        submitButton.setOnClickListener {
            postComment()
        }
    }

    private fun postComment() {
        val receptionId = tokenManager.getReceptionId() // Get reception ID
        val accessToken = tokenManager.getAccessToken() // Get access token
        val commentDescription = commentEditText.text.toString()

        if (receptionId != null && accessToken != null && commentDescription.isNotEmpty()) {
            apiService.addComment("Bearer $accessToken", receptionId, commentDescription).enqueue(object : Callback<ReceptionCommentResponse> {
                override fun onResponse(call: Call<ReceptionCommentResponse>, response: Response<ReceptionCommentResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AmbulanceAddCommentActivity, "댓글 등록 성공", Toast.LENGTH_SHORT).show()
                        finish() // Optionally close the activity after submission
                    } else {
                        Toast.makeText(this@AmbulanceAddCommentActivity, "댓글 등록 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReceptionCommentResponse>, t: Throwable) {
                    Toast.makeText(this@AmbulanceAddCommentActivity, "오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
