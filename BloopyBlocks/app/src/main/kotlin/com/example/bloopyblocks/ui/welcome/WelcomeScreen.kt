package com.example.bloopyblocks.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloopyblocks.R
import com.example.bloopyblocks.ui.theme.BloopBodyFont

import com.example.bloopyblocks.ui.theme.BloopCream
import com.example.bloopyblocks.ui.theme.BloopDark
import com.example.bloopyblocks.ui.theme.BloopGray
import com.example.bloopyblocks.ui.theme.BloopTeal
import com.example.bloopyblocks.ui.theme.BloopTitleFont

@Composable
fun WelcomeScreen(onStart: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloopCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.bloop),
            contentDescription = "Bloop" ,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Bloopy Blocks",
            fontSize = 32.sp,
            fontFamily = BloopTitleFont,
            color = BloopDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Draw blocks. Build your world.",
            fontSize = 16.sp,
            fontFamily = BloopBodyFont,
            color = BloopGray
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BloopTeal)
        ) {
            Text(
                text = "Start Building",
                fontSize = 18.sp,
                fontFamily = BloopBodyFont
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onStart = {})
}