package com.example.sos.ambulance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sos.Location
import com.example.sos.R
import com.example.sos.retrofit.KakaoRetrofitClientInstance
import com.example.sos.retrofit.RegisterRequest
import com.example.sos.retrofit.RegisterResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.AmbulanceIdDupCheckResponse
import com.example.sos.token.TokenManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.sos.KeywordSearchResponse

class RegisterAmbulanceActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private var selectedImageUri: Uri? = null
    private var isIdChecked = false // 아이디 중복 확인 여부

    // 이미지 선택을 처리할 ActivityResultLauncher
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "이미지 선택됨: ${uri.path}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "이미지 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_register)

        tokenManager = TokenManager(this)

        val idEditText = findViewById<EditText>(R.id.editTextUserId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val nameEditText = findViewById<EditText>(R.id.editTextAmbulanceName)
        val phoneEditText = findViewById<EditText>(R.id.editTextAmbulancePhone)
        val addressEditText = findViewById<EditText>(R.id.editTextAmbulanceAddress)
        val checkDupButton = findViewById<Button>(R.id.buttonCheckDup)
        val selectImageButton = findViewById<Button>(R.id.buttonSelectImage)
        val submitButton = findViewById<Button>(R.id.buttonSubmit)

        // 아이디 중복 확인
        checkDupButton.setOnClickListener {
            val id = idEditText.text.toString()
            if (!isValidId(id)) {
                Toast.makeText(this, "아이디는 영어와 숫자만 포함해야 합니다.", Toast.LENGTH_SHORT).show()
            } else {
                checkIdDuplication(id)
            }
        }

        // 이미지 선택
        selectImageButton.setOnClickListener {
            getImage.launch("image/*")
        }

        // 회원가입
        submitButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val name = nameEditText.text.toString()
            val telephoneNumber = phoneEditText.text.toString()
            val address = addressEditText.text.toString()

            // 입력값 검증
            if (!isIdChecked) {
                Toast.makeText(this, "아이디 중복 확인을 완료해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPhone(telephoneNumber)) {
                Toast.makeText(this, "전화번호는 올바른 형식이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                searchLocationAndRegister(id, password, name, address, telephoneNumber)
            }
        }
    }

    private suspend fun searchLocationAndRegister(
        id: String,
        password: String,
        name: String,
        address: String,
        telephoneNumber: String
    ) {
        val kakaoService = KakaoRetrofitClientInstance.kakaoService
        try {
            val apiKey = "KakaoAK ${KakaoRetrofitClientInstance.API_KEY}" // 헤더에 API 키 추가
            Log.d("KakaoAPI", "키워드 검색 요청: $address")
            val response = kakaoService.searchKeyword(apiKey, address)

            if (response.isSuccessful) {
                val documents = response.body()?.documents
                Log.d("KakaoAPI", "응답 성공: $documents")
                if (!documents.isNullOrEmpty()) {
                    val address = documents[0].road_address_name
                    val latitude = documents[0].y
                    val longitude = documents[0].x

                    val location = Location(latitude, longitude)

                    uploadImageAndRegister(id, password, name, address?:"오류", telephoneNumber, location)
                } else {
                    Log.w("KakaoAPI", "검색 결과가 없습니다.")
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("KakaoAPI", "API 호출 실패: ${response.code()} - $errorBody")
                Toast.makeText(this, "키워드 검색 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("KakaoAPI", "키워드 검색 중 예외 발생: ${e.message}")
        }
    }



    // 아이디 유효성 검증
    private fun isValidId(id: String): Boolean {
        return id.matches("^[a-zA-Z0-9]+$".toRegex())
    }

    // 전화번호 유효성 검증
    private fun isValidPhone(phone: String): Boolean {
        return phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$".toRegex())
    }

    // 구급대 이름 유효성 검증
    private fun isValidName(name: String): Boolean {
        return name.matches("^[가-힣a-zA-Z ]+$".toRegex())
    }

    // 이미지 업로드 및 회원가입
    private fun uploadImageAndRegister(
        id: String,
        password: String,
        name: String,
        address: String,
        telephoneNumber: String,
        location: Location
    ) {
        if (selectedImageUri != null) {
            val storageReference = FirebaseStorage.getInstance()
                .reference.child("images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(selectedImageUri!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val registerRequest = RegisterRequest(
                        id, password, name, address, telephoneNumber, location, imageUrl
                    )
                    registerAmbulance(registerRequest)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 회원가입 요청
    private fun registerAmbulance(registerRequest: RegisterRequest) {
        val authService = RetrofitClientInstance.getApiService(tokenManager)
        authService.register(registerRequest).enqueue(object : retrofit2.Callback<RegisterResponse> {
            override fun onResponse(call: retrofit2.Call<RegisterResponse>, response: retrofit2.Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 아이디 중복 체크 요청
    private fun checkIdDuplication(id: String) {
        val authService = RetrofitClientInstance.getApiService(tokenManager)
        authService.checkIdDuplication(id, "AMB").enqueue(object : retrofit2.Callback<AmbulanceIdDupCheckResponse> {
            override fun onResponse(call: retrofit2.Call<AmbulanceIdDupCheckResponse>, response: retrofit2.Response<AmbulanceIdDupCheckResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterAmbulanceActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                    isIdChecked = true
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "중복된 ID 입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<AmbulanceIdDupCheckResponse>, t: Throwable) {
                Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
