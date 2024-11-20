package com.example.sos.res

data class ReceptionRes(
    val id: String,
    val startTime: String,
    val endTime: String,
    val ambulance: AmbulanceRes,
    val hospital: HospitalRes,
    val patient: PatientRes,
    val comments: List<CommentRes>,
    val receptionStatus: String?,
    val paramedicRes: ParamedicsRes
)
