package com.fintrack.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fintrack.shared.feature.transaction.ui.IncomeTrackerScreen
import com.fintrack.shared.App

//import com.fintrack.app.App


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            IncomeTrackerScreen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    IncomeTrackerScreen()
}