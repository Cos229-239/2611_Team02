package com.example.bloopyblocks.data.remote
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FireBaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    fun signInAnonymously(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        val currentUser = auth.currentUser
        if(currentUser!=null){
            onSuccess(currentUser.uid)
            return
        }

        auth.signInAnonymously().addOnSuccessListener {
            result->
            val userId = result.user!!.uid
            onSuccess(userId)

        }.addOnFailureListener {
            exception ->
            onFailure(exception)
        }
    }
    suspend fun getUserId(): String{
        val currentUser = auth.currentUser
        if(currentUser!=null){
            return currentUser.uid
        }
        val result = auth.signInAnonymously().await()
        return result.user!!.uid
    }
}