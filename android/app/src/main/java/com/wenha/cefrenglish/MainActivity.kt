package com.wenha.cefrenglish

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.wenha.cefrenglish.app.CefrEnglishApp
import com.wenha.cefrenglish.ui.common.LearningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LearningTheme {
                CefrEnglishApp()
            }
        }
    }
}
