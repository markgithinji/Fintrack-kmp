package com.fintrack.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.fintrack.shared.feature.navigation.MainScreen
import com.fintrack.shared.feature.settings.domain.util.initBiometricAuthenticator


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        initBiometricAuthenticator(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MainScreen()
}