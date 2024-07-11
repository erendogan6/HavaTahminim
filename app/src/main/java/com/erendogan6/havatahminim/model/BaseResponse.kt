package com.erendogan6.havatahminim.model

data class BaseResponse(
    val weather: List<Weather>,
    val main: Main,
    val dt: Long,
    val sys: Sys,
    val name: String,
)