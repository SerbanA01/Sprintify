package com.example.runpath.database

import androidx.compose.animation.core.snap
import com.example.runpath.models.Community_Users
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject

class Community_UsersDAO{
    private val db = FirebaseFirestore.getInstance()

    fun insertCommunity_Users(community_users: Community_Users, onComplete: (Community_Users) -> Unit) {
        val documentReference = db.collection("community_users").document()
        val community_usersId = documentReference.id
        val newCommunity_Users = community_users.copy(community_usersID = community_usersId)

        documentReference.set(newCommunity_Users)
            .addOnSuccessListener {
                onComplete(newCommunity_Users)
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    fun getCommunity_UsersById(community_usersId: String) {
        db.collection("community_users")
            .document(community_usersId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    println("${document.id} => ${document.data}")
                } else {
                    println("No such document")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting document: $exception")
            }
    }

    fun getCommunity_Users(onComplete: (List<Community_Users>) -> Unit){
        db.collection("community_users")
            .get()
            .addOnSuccessListener { documents ->
                val community_users = documents.map { it.toObject<Community_Users>().copy(community_usersID = it.id) }
                onComplete(community_users)
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

    fun listenForCommunity_Users(onCommunity_UsersUpdated: (List<Community_Users>) -> Unit): ListenerRegistration {
        return db.collection("community_users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val community_users = snapshots.map { it.toObject<Community_Users>().copy(community_usersID = it.id) }
                    onCommunity_UsersUpdated(community_users)
                }
            }
    }

    fun updateCommunity_Users(
        community_usersId: String,
        userId: String,
        dateJoined: String
    ){
        val community_users = Community_Users(
            community_usersID = community_usersId,
            userID = userId,
            dateJoined = dateJoined
        )

        db.collection("community_users")
            .document(community_usersId)
            .set(community_users)
            .addOnSuccessListener {
                println("DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    fun deleteCommunity_Users(community_usersId: String){
        db.collection("community_users")
            .document(community_usersId)
            .delete()
            .addOnSuccessListener {
                println("DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                println("Error deleting document: $e")
            }
    }
}

