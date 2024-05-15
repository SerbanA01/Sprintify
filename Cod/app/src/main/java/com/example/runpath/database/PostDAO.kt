package com.example.runpath.database

import com.example.runpath.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject

class PostDAO {
    private val db = FirebaseFirestore.getInstance()

    fun insertPost(post: Post, onComplete: (Post) -> Unit) {
        val documentReference = db.collection("posts").document()
        val postId = documentReference.id
        val newPost = post.copy(postId = postId)

        documentReference.set(newPost)
            .addOnSuccessListener {
                onComplete(newPost)
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    fun getPostById(postId: String) {
        db.collection("posts")
            .document(postId)
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

    fun getPosts(onComplete: (List<Post>) -> Unit) {
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { it.toObject<Post>().copy(postId = it.id) }
                onComplete(posts)
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

    fun listenForPosts(onPostsUpdated: (List<Post>) -> Unit): ListenerRegistration {
        return db.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val posts = snapshots.map { it.toObject<Post>().copy(postId = it.id) }
                    onPostsUpdated(posts)
                }
            }
    }

    fun updatePost(
        postId: String,
        userId: Int,
        author: String,
        timestamp: String,
        content: String
    ) {
        val post = Post(
            userId = userId,
            author = author,
            timestamp = timestamp,
            content = content,
            postId = postId
        )

        db.collection("posts")
            .document(postId)
            .set(post)
            .addOnSuccessListener { println("DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> println("Error updating document: $e") }
    }

    fun deletePost(postId: String) {
        db.collection("posts")
            .document(postId)
            .delete()
            .addOnSuccessListener { println("DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e ->
                println("Error deleting document: $e")
            }
    }
}
