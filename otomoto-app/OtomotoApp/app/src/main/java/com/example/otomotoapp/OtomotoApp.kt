package com.example.otomotoapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun aaa() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "jfaaaaaaaaaaaaaaaajfaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaahbdaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaahbd",
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp), // отступ справа под иконку
        )
        Icon(
            painter = painterResource(R.drawable.engine),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp) // немного отступов от краёв
        )
    }
}



@Preview(showBackground = true)
@Composable
fun PrevAA() {
    aaa()
}

