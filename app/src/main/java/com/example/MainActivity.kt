package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.database.AppDatabase
import com.example.data.repository.RecentFilesRepository
import com.example.data.repository.ScratchpadRepository
import com.example.ui.EditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.EditorViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getInstance(applicationContext) }
    private val recentFilesRepo by lazy { RecentFilesRepository(database.recentFileDao()) }
    private val scratchpadRepo by lazy { ScratchpadRepository(database.scratchpadDao()) }

    private val viewModel: EditorViewModel by viewModels {
        EditorViewModelFactory(recentFilesRepo, scratchpadRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                EditorScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val dataUri: Uri? = intent.data

        when (action) {
            Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                if (dataUri != null) {
                    viewModel.handleIncomingUri(this, dataUri)
                }
            }
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type || "text/markdown" == intent.type) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    val streamUri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)

                    if (streamUri != null) {
                        viewModel.handleIncomingUri(this, streamUri)
                    } else if (!sharedText.isNullOrEmpty()) {
                        viewModel.updateContent(sharedText)
                    }
                }
            }
        }
    }
}
