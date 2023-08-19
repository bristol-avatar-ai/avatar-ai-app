package com.example.avatar_ai_app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.avatar_ai_app.R
import com.example.avatar_ai_app.ui.theme.ARAppTheme

@Composable
fun EnableCameraButton(
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }

    val buttonColor by rememberUpdatedState(
        if (interactionSource.collectIsPressedAsState().value) Color.DarkGray else Color.Gray
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.error_icon),
            contentDescription = "Error Icon",
            modifier = Modifier.size(50.dp)
        )
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = stringResource(id = R.string.camera_permissions_declined_screen)
        )
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            interactionSource = interactionSource
        ) {
            Text(text = stringResource(id = R.string.grant_camera_access_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    ARAppTheme {
        EnableCameraButton(onClick = {})
    }
}

