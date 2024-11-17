package com.example.sos.res

import java.time.LocalDateTime

data class ReceptionRes(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val ambulance: AmbulanceRes,
    val hospital: HospitalRes,
    val patient: PatientRes,
    val comments: List<CommentRes>,
    val receptionStatus: String?,
    val paramedicRes: ParamedicsRes
)
