package com.example.mobiledialoga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobiledialoga.navigation.AppNavigation
import com.example.mobiledialoga.ui.theme.MobileDialogaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileDialogaTheme {
                AppNavigation()
            }
        }
    }
}
