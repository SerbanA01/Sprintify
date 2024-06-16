package com.example.runpath.ui.theme.ProfileAndCommunity

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.runpath.database.CommunityDAO
import com.example.runpath.database.PostDAO
import com.example.runpath.models.Community
import com.example.runpath.models.Post
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale

@Composable
fun GeneralPage(userId: String, username: String, navController: NavController) {
    var text by remember { mutableStateOf("Community") }
    var posts by remember { mutableStateOf(listOf<Post>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf("") }
    var communityId by remember { mutableStateOf("") }
    val communityDAO = CommunityDAO()
    var joinedCommunities by remember { mutableStateOf(listOf<Community>())}
    var isMember by remember { mutableStateOf(false) }
    var selectedCommunity by remember { mutableStateOf<Community?>(null)}
    var showDropdown by remember { mutableStateOf(false) }

    val postDAO = PostDAO()

    // Listener for posts
    DisposableEffect(Unit) {
        val listenerRegistration = postDAO.listenForPosts { updatedPosts ->
            posts = updatedPosts
        }

        onDispose {
            listenerRegistration.remove()
        }
    }

    DisposableEffect(userId) {
        communityDAO.getCommunities { allCommunities ->
            allCommunities.forEach { community ->
                communityDAO.isUserMemberOfCommunity(community.communityId ?: "", userId) { isUserMember ->
                    if (isUserMember) {
                        joinedCommunities = joinedCommunities + community
                        isMember = true
                    }
                }
            }
        }
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8A2BE2))
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .padding(top = 58.dp, bottom = 58.dp)
        ) {
            itemsIndexed(posts) { index, post ->
                if (joinedCommunities.map { it.communityId }.contains(post.communityId)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .clickable {
                                Log.d("Navigation", "Navigating to userProfile/${post.author}")
                                navController.navigate("userProfile/${post.author}")
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = post.author,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    Log.d("Navigation", "Navigating to userProfile/${post.author}")
                                    navController.navigate("userProfile/${post.author}")
                                }
                            )
                            Text(
                                text = formatDate2(post.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(text = "\n${post.content}")

                            var communityName by remember { mutableStateOf("") }
                            DisposableEffect(post.communityId) {
                                communityDAO.getCommunityName(post.communityId) { name ->
                                    communityName = name
                                }
                                onDispose { }
                            }

                            Text(text = "Posted in: $communityName")
                        }

                        if (post.author == username) {
                            IconButton(
                                onClick = {
                                    post.postId?.let { postDAO.deletePost(it) }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        Color(0xFF8A2BE2),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Delete Post",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showDialog = true },
            enabled = isMember,
            modifier = Modifier
                .padding(bottom = 58.dp, end = 8.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text("Create Post")
        }

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

                    // Add this button to open the dropdown menu
                    Button(onClick = { showDropdown = true }) {
                        Text(selectedCommunity?.name ?: "Select a community")
                    }

                    // Modify this dropdown menu to only close when a community is selected
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        joinedCommunities.forEach { community ->
                            DropdownMenuItem(onClick = {
                                selectedCommunity = community
                                communityId = community.communityId ?: ""
                                showDropdown = false
                            }) {
                                Text(community.name)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val post = Post(
                                userId = userId ?: "N/A",
                                author = username,
                                content = newPostContent,
                                timestamp = LocalDateTime.now().toString(),
                                communityId = communityId
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

fun formatDate2(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timestamp)
        sdf.format(date)
    } catch (e: Exception) {
        "Invalid Date"
    }
}
