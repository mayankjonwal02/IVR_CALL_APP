package com.example.ivr_call_app_20

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform