package com.yei.dev.controlerinterrapidisimo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yei.dev.controlerinterrapidisimo.R

/**
 * Dialog type for InfoDialog.
 */
enum class DialogType {
    ERROR,
    INFO
}

/**
 * Reusable dialog component for displaying information or error messages.
 *
 * @param type The type of dialog (ERROR or INFO)
 * @param message The message to display in the dialog
 * @param onDismiss Callback when the dialog is dismissed
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun InfoDialog(
    type: DialogType,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (type) {
                    DialogType.ERROR -> Icons.Default.Error
                    DialogType.INFO -> Icons.Default.Info
                },
                contentDescription = when (type) {
                    DialogType.ERROR -> "Error"
                    DialogType.INFO -> "Information"
                },
                tint = when (type) {
                    DialogType.ERROR -> Color.Red
                    DialogType.INFO -> colorResource(R.color.orange)
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = when (type) {
                    DialogType.ERROR -> "Error"
                    DialogType.INFO -> "Información"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            // Make text scrollable for long messages
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (type) {
                        DialogType.ERROR -> Color.Red
                        DialogType.INFO -> colorResource(R.color.orange)
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Salir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        modifier = modifier
    )
}


// Preview composables
@Composable
private fun InfoDialogPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error dialog preview
        InfoDialog(
            type = DialogType.ERROR,
            message = "No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet e intenta nuevamente.",
            onDismiss = {}
        )
    }
}
