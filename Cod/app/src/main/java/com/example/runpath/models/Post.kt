package com.example.runpath.models

data class Post(
    val userId: String = " ",
    val postId: String? = null,
    val author: String = "",
    val timestamp: String = "",
    val content: String = "",
    val communityId: String = ""
)