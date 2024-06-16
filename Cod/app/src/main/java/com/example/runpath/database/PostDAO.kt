package com.example.runpath.database

import com.example.runpath.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject

class PostDAO {
    // creez o noua instanta a bazei de date
    private val db = FirebaseFirestore.getInstance()
    // creez un nou post
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
    // obtin un post dupa id
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
    // obtin toate posturile
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
    // creez un listener pentru posturi
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
    // actualizez un post
    fun updatePost(
        postId: String,
        userId: String,
        author: String,
        timestamp: String,
        content: String,
        communityId: String
    ) {
        val post = Post(
            userId = userId,
            postId = postId,
            author = author,
            timestamp = timestamp,
            content = content,
            communityId = communityId
        )

        db.collection("posts")
            .document(postId)
            .set(post)
            .addOnSuccessListener { println("DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> println("Error updating document: $e") }
    }
    // sterg un post
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