package com.bbrustol.mmm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bbrustol.core.ui.theme.MindMyAppTheme
import com.bbrustol.feature.organizations.ui.NavOrganizations
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindMyAppTheme {
                KoinContext {
                    NavOrganizations()
                }
            }
        }
    }
}
