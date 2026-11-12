package com.example.bloopyblocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bloopyblocks.ui.theme.BloopyBlocksTheme
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.example.bloopyblocks.data.remote.FireBaseAuthManager


class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val authManager = FireBaseAuthManager()
    authManager.signInAnonymously(onSuccess = {userId->Log.d("FireBaseAuth", "Signed in with user id $userId")}, onFailure={exception -> Log.e("FireBaseAuth", "Sign in Failed", exception)})
    enableEdgeToEdge()
    setContent {

      BloopyBlocksTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}

