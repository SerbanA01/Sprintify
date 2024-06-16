package com.example.runpath.ui.theme.ProfileAndCommunity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.runpath.database.CommunityDAO
import com.example.runpath.models.Community

@Composable
fun DiscoverPage(userId: String, navController: NavController) {
    val communityDAO = CommunityDAO()
    var communities by remember { mutableStateOf(listOf<Community>()) }
    var joinedCommunities by remember { mutableStateOf(listOf<Community>()) }
    var isMember by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var newCommunityName by remember { mutableStateOf("") }
    var newCommunityDescription by remember { mutableStateOf("") }

    // Listener for communities
    DisposableEffect(Unit) {
        val listenerRegistration = communityDAO.listenForCommunities { updatedCommunities ->
            communities = updatedCommunities
        }

        onDispose {
            listenerRegistration.remove()
        }
    }

//    // Listener for joined communities
//    DisposableEffect(userId) {
//        val listenerRegistration = communityDAO.listenForJoinedCommunities(userId) { updatedJoinedCommunities ->
//            joinedCommunities = updatedJoinedCommunities
//        }
//
//        onDispose {
//            listenerRegistration.remove()
//        }
//    }


    // Fetch joined communities
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

    Column {
        Text(
            "Available Communities",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn {
            itemsIndexed(communities) { index, community ->
                var isMemberOfThisCommunity by remember { mutableStateOf(false) }

                DisposableEffect(community.communityId) {
                    communityDAO.isUserMemberOfCommunity(community.communityId ?: "", userId) { isUserMember ->
                        isMemberOfThisCommunity = isUserMember
                    }
                    onDispose { }
                }

                Row {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.weight(1f)

                    )
                    Button(
                        onClick = { communityDAO.joinCommunity(community.communityId ?: "", userId) },
                        enabled = !isMemberOfThisCommunity
                    ) {
                        Text("Join")
                    }
                }
            }
        }

        Text(
            "Joined Communities",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn {
            itemsIndexed(joinedCommunities) { index, community ->
                Row {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { communityDAO.leaveCommunity(community.communityId ?: "", userId) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Leave Community"
                        )
                    }
                }
            }
        }

        // Button to trigger dialog for creating a new community
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(bottom = 58.dp, end = 8.dp)

        ) {
            Text("Create Community")
        }
    }

    // Dialog for creating a new community
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
                    value = newCommunityName,
                    onValueChange = { newCommunityName = it },
                    label = { Text("Community Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = newCommunityDescription,
                    onValueChange = { newCommunityDescription = it },
                    label = { Text("Community Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val community = Community(
                            name = newCommunityName,
                            description = newCommunityDescription
                        )
                        communityDAO.insertCommunity(community) {
                            newCommunityName = ""
                            newCommunityDescription = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Create Community")
                }
            }
        }
    }
}