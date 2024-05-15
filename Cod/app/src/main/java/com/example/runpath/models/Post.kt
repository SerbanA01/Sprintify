package com.example.runpath.models;

data class Post(
    val userId: Int = 0,
    val postId: String? = null,
    val author: String = "",
    val timestamp: String = "",
    val content: String = ""
)