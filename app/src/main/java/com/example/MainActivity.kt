package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.ABKDatabase
import com.example.data.repository.ABKRepository
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ABKViewModel
import com.example.ui.viewmodel.ABKViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Local Database
        val database = ABKDatabase.getDatabase(this)
        val dao = database.dao()

        // 2. Initialize Persistent Repository
        val repository = ABKRepository(dao)

        // 3. Create state ViewModel via factory
        val viewModel = ViewModelProvider(
            this,
            ABKViewModelFactory(repository)
        )[ABKViewModel::class.java]

        setContent {
            MyApplicationTheme {
                // Render the unified, luxury, premium cinematic app
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}
