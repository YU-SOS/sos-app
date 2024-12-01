package com.example.sos

data class KeywordSearchResponse(
    val documents: List<Document>
)

data class Document(
    val place_name: String,         // 장소 이름
    val road_address_name: String?, // 도로명 주소 (Optional)
    val address_name: String?,      // 지번 주소 (Optional)
    val x: String,                  // 경도
    val y: String                   // 위도
)

