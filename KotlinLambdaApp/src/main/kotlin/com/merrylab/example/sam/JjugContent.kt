package com.merrylab.example.sam

import kotlinx.serialization.Serializable

@Serializable
data class JjugContent(val title: String, val category: String, val speaker: String, val start_time: Long)
