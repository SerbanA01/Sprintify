package com.example.runpath.models;

data class Circuit (
    val circuitId: Int,
    val name: String,
    val description: String,
    val distance: Double,
    val estimatedTime: String,
    val intensity: Int,
    val terrain: String,
    val petFriendly: Boolean,
    val lightlevel: Int,
    val rating: Double,
    val difficulty: Int
)