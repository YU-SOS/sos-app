package com.example.sos

import java.time.LocalDateTime

data class CommentRes(
    val id: Int,
    val content: String,
    val createdAt: LocalDateTime
)