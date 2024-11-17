package com.example.sos.res

import java.time.LocalDateTime

data class CommentRes(
    val id: Int,
    val content: String,
    val createdAt: LocalDateTime
)