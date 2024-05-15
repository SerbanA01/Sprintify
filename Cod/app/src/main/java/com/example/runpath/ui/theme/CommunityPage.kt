package com.example.runpath.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.time.LocalDateTime
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import com.example.runpath.database.PostDAO
import com.example.runpath.database.SessionManager
import com.example.runpath.models.Post


@Composable
fun CommunityPage(navController: NavController, sessionManager: SessionManager) {
    val sharedPreferences = sessionManager.getsharedPreferences()
    var text by remember { mutableStateOf("Community") }
    val username = sharedPreferences.getString("username", "N/A") ?: "N/A"
    val userId = sharedPreferences.getInt("user_id", -1)
    var posts by remember { mutableStateOf(listOf<Post>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf("") }

    val postDAO = PostDAO()

    // Set up real-time listener for posts
    DisposableEffect(Unit) {
        val listenerRegistration = postDAO.listenForPosts { updatedPosts ->
            posts = updatedPosts
        }

        onDispose {
            listenerRegistration.remove()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        // Posts feed
        LazyColumn(
            modifier = Modifier
                .padding(top = 58.dp)
        ) {
            itemsIndexed(posts) { index, post ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${post.author}: ${post.content} at ${post.timestamp}")



                    if (post.author == username) {
                        //update post button

                        //delete post button
                        IconButton(onClick = {
                            post.postId?.let { postDAO.deletePost(it) }
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete Post")
                        }

                    }


                }
            }
        }

        // Button to create a new post
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(bottom = 58.dp, end = 8.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text("Create Post")
        }

        // Dialog to create a new post
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = newPostContent,
                        onValueChange = { newPostContent = it },
                        label = { Text("Post Content") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val post = Post(
                                userId = userId,
                                author = username,
                                content = newPostContent,
                                timestamp = LocalDateTime.now().toString()
                            )
                            postDAO.insertPost(post) {
                                newPostContent = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Create Post")
                    }
                }
            }
        }
    }
}
