package com.example.uzradyab.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class SnackbarType {
    SUCCESS, ERROR, INFO
}

interface AppSnackbarController {
    fun showSuccess(message: String)
    fun showError(message: String)
    fun showInfo(message: String)
}

class AppSnackbarControllerImpl(
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) : AppSnackbarController {
    override fun showSuccess(message: String) {
        show("[SUCCESS]$message")
    }

    override fun showError(message: String) {
        show("[ERROR]$message")
    }

    override fun showInfo(message: String) {
        show("[INFO]$message")
    }

    private fun show(prefixedMessage: String) {
        coroutineScope.launch {
            // Cancel current snackbar if any
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = prefixedMessage,
                duration = SnackbarDuration.Short
            )
        }
    }
}

val LocalSnackbarController = compositionLocalOf<AppSnackbarController> {
    error("No LocalSnackbarController provided")
}

@Composable
fun GlobalSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            val message = data.visuals.message
            val (type, displayMessage) = when {
                message.startsWith("[SUCCESS]") -> SnackbarType.SUCCESS to message.removePrefix("[SUCCESS]")
                message.startsWith("[ERROR]") -> SnackbarType.ERROR to message.removePrefix("[ERROR]")
                message.startsWith("[INFO]") -> SnackbarType.INFO to message.removePrefix("[INFO]")
                else -> SnackbarType.INFO to message
            }

            val containerColor = when (type) {
                SnackbarType.SUCCESS -> Color(0xFF4CAF50)
                SnackbarType.ERROR -> Color(0xFFF44336)
                SnackbarType.INFO -> Color(0xFF6A8BA5) // Theme secondary/info color
            }

            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = containerColor,
                contentColor = Color.White,
                actionContentColor = Color.White,
                actionOnNewLine = false,
            ) {
                androidx.compose.material3.Text(
                    text = displayMessage,
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
