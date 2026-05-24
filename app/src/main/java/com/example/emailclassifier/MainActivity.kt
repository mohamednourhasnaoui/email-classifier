package com.example.emailclassifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emailclassifier.ui.screens.EmailClassifierScreen
import com.example.emailclassifier.ui.theme.EmailClassifierTheme
import com.example.emailclassifier.viewmodel.EmailClassifierViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmailClassifierTheme {
                val viewModel: EmailClassifierViewModel = viewModel()
                EmailClassifierScreen(viewModel = viewModel)
            }
        }
    }
}