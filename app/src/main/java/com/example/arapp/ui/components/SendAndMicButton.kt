package com.example.arapp.ui.components

import android.Manifest
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SendAndMicButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    icon: Painter,
    description: String = "",
    textFieldFocusState: Boolean,
    recordingPermissionsEnabled: Boolean,
) {
    val pressed = remember { mutableStateOf(false) }

    val buttonColor by rememberUpdatedState(
        if (pressed.value) Color.DarkGray else Color.Transparent
    )

    val iconTint by rememberUpdatedState(
        if (pressed.value) Color.White else Color.Black
    )

    val isTextInputMode by rememberUpdatedState(newValue = textFieldFocusState)

    val permissionsState by rememberUpdatedState(newValue = recordingPermissionsEnabled)

    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(all = 8.dp)
            .background(color = buttonColor, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            if (permissionsState || isTextInputMode) {
                                onPress()
                            } else {
                                permissionLauncher.launch(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            }
                            pressed.value = true
                            awaitRelease()
                        } finally {
                            if (permissionsState || isTextInputMode) {
                                onRelease()
                            }
                            pressed.value = false
                        }
                    }
                )
            }
    ) {
        Icon(
            icon,
            description,
            Modifier.fillMaxSize(),
            iconTint
        )
    }
}