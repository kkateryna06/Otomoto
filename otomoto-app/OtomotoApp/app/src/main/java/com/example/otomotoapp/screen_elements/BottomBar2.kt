package com.example.otomotoapp.screen_elements

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.otomotoapp.AppBarsViewModel

@Composable
fun BottomBar2(appBarsViewModel: AppBarsViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appBarsViewModel.carLink))
            context.startActivity(intent)
        }) {
            Text(text = "View in Otomoto")
        }
        Text(text = "${appBarsViewModel.carPrice} PLN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}
