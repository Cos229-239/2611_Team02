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



class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    auth = FirebaseAuth.getInstance()
    if (auth.currentUser == null) {
      auth.signInAnonymously()
        .addOnCompleteListener(this) { task ->
          if (task.isSuccessful) {
            val user = auth.currentUser
            Log.d("BloopyBlocks", "Anonymous sign-in successful!")
            Log.d("BloopyBlocks", "User ID: ${user?.uid}")
          } else {
            Log.e(
              "BloopyBlocks",
              "Anonymous sign-in failed",
              task.exception
            )
          }
        }
    }
    enableEdgeToEdge()
    setContent {

      BloopyBlocksTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}

