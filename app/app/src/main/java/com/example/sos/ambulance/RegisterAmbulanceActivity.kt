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
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.RegisterRequest
import com.example.sos.retrofit.RegisterResponse
import com.example.sos.retrofit.AmbulanceIdDupCheckResponse
import com.example.sos.token.TokenManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAmbulanceActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private var selectedImageUri: Uri? = null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "이미지 선택됨", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_ambulance)

        tokenManager = TokenManager(this)

        val idEditText = findViewById<EditText>(R.id.editTextUserId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val nameEditText = findViewById<EditText>(R.id.editTextAmbulanceName)
        val phoneEditText = findViewById<EditText>(R.id.editTextAmbulancePhone)
        val addressEditText = findViewById<EditText>(R.id.editTextAmbulanceAddress)
        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        val selectImageButton = findViewById<Button>(R.id.buttonSelectImage)
        val checkDupButton = findViewById<Button>(R.id.buttonCheckDup)

        selectImageButton.setOnClickListener {
            getImage.launch("image/*")
        }

        checkDupButton.setOnClickListener {
            val id = idEditText.text.toString()
            if (id.isNotEmpty()) {
                checkIdDuplication(id)
            } else {
                Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        submitButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val name = nameEditText.text.toString()
            val telephoneNumber = phoneEditText.text.toString()
            val address = addressEditText.text.toString()

            if (id.isEmpty() || password.isEmpty() || name.isEmpty() || telephoneNumber.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val kakaoService = KakaoRetrofitClientInstance.kakaoService
                val response = kakaoService.searchAddress("KakaoAK 24a76ea9dc5ffd6677de0900eedb7f72", address)
                if (response.isSuccessful) {
                    val documents = response.body()?.documents
                    if (!documents.isNullOrEmpty()) {
                        val latitude = documents[0].y
                        val longitude = documents[0].x
                        val location = Location(latitude, longitude)
                        uploadImageAndRegister(id, password, name, address, telephoneNumber, location)
                    } else {
                        Log.d("RegisterAmbulanceActivity", "주소 검색 실패")
                    }
                } else {
                    Log.d("RegisterAmbulanceActivity", "주소 검색 실패 : ${response.message()}")
                }
            }
        }
    }

    private fun checkIdDuplication(id: String) {
        val authService = RetrofitClientInstance.getApiService(tokenManager)
        authService.checkIdDuplication(id, "AMB").enqueue(object : Callback<AmbulanceIdDupCheckResponse> {
            override fun onResponse(call: Call<AmbulanceIdDupCheckResponse>, response: Response<AmbulanceIdDupCheckResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message
                    Toast.makeText(this@RegisterAmbulanceActivity, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "중복된 ID 입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AmbulanceIdDupCheckResponse>, t: Throwable) {
                Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadImageAndRegister(id: String, password: String, name: String, address: String, telephoneNumber: String, location: Location) {
        if (selectedImageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.reference.child("images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(selectedImageUri!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    registerAmbulance(RegisterRequest(id, password, name, address, telephoneNumber, location, imageUrl))
                }.addOnFailureListener {
                    Toast.makeText(this, "이미지 URL 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerAmbulance(registerRequest: RegisterRequest) {
        val authService = RetrofitClientInstance.getApiService(tokenManager)
        authService.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.status == 201) {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterAmbulanceActivity, AmbulanceMainActivity::class.java))
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
