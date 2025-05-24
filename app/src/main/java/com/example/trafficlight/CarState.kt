package com.example.trafficlight

enum class CarState {
    RED,
    RED_YELLOW,
    GREEN,
    YELLOW;

    fun isRed() = this == RED || this == RED_YELLOW
    fun isYellow() = this == RED_YELLOW || this == YELLOW
    fun isGreen() = this == GREEN
}