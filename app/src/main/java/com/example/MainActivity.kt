package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.NairaGuardApp
import com.example.ui.NairaGuardViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NairaGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handlePaymentIntent(intent)

        setContent {
            MyApplicationTheme {
                NairaGuardApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentIntent(intent)
    }

    private fun handlePaymentIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && (data.scheme == "nairaguard" || data.toString().contains("payment-callback"))) {
            viewModel.handleSquadPaymentCallback(data) { success, message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
