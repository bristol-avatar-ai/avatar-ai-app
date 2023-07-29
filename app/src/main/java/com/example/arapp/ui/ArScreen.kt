package com.example.arapp.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arapp.R
import com.example.arapp.ui.theme.ARAppTheme
import io.github.sceneview.ar.ARScene

@Composable
fun ArScreen(
    arViewModel: ArViewModel = viewModel()
) {
    val arUiState by arViewModel.uiState.collectAsState()
    //Ar screen scope
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val touchPosition by remember { arViewModel.touchPosition }
    val focusRequester = remember { arViewModel.focusRequester }

    Box(
        Modifier.fillMaxHeight()
    ) {
        if (arUiState.isLoading) {
            LoadingScreen()
        } else {
            ARScene(
                modifier = Modifier
                    .fillMaxSize(),
                nodes = remember { arViewModel.nodes },
                planeRenderer = false,
                onCreate = { arSceneView ->
                    arViewModel.addAvatarToScene(arSceneView, scope, context)
                },
            )
            //Allows detection of touches on the ARScene
            BoxWithConstraints(
                Modifier
                    .fillMaxHeight(0.95f)
                    .fillMaxWidth(0.95f)
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                arViewModel.dismissActionMenu()
                                focusRequester.requestFocus()
                            }
//                        onLongPress = { press ->
//                            arViewModel.arSceneOnLongPress(press)
//                        }
                        )
                    }
                    .focusRequester(focusRequester)
                    .focusable()
            ) {
//            val constraints = this
//            val offSet = arViewModel.generateCoordinates(constraints, touchPosition)
            }
            Column(
                Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.End)
                ) {
                    Box(
                        Modifier
                            .align(Alignment.Bottom)
                            .fillMaxWidth(
                                if (arUiState.isAvatarMenuVisible) {
                                    0.5f
                                } else 0.8f
                            )
                    ) {
                        if (arUiState.responsePresent) {
                            TextResponse(
                                arUiState = arUiState,
                                onClick = { arViewModel.dismissTextResponse() }
                            )
                        }
                    }
                    Box(
                        Modifier
                            .align(Alignment.Bottom)
                    ) {
                        FloatingActionMenu(arViewModel)
                    }
                }
                BottomBar(arViewModel, arUiState)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Loading",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun TextResponse(
    arUiState: ArUiState,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .padding(5.dp)
            .background(
                color = colorResource(R.color.text_response),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = arUiState.responseValue,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(5.dp),

            )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    arViewModel: ArViewModel,
    arUiState: ArUiState
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        arViewModel.textState
    }

    var textFieldFocusState by remember { arViewModel.textFieldFocusState }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.Gray,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                Modifier.size(5.dp)
            )
            UserInput(
                textFieldValue = textState,
                onTextChanged = { textState = it },
                onSend = { arViewModel.onSend() },
                placeHolderText = { Text(text = "Ask me a question!") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.LightGray,
                ),
                modifier = Modifier.weight(1f),
                onTextFieldFocused = { focused: Boolean ->
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState
            )
            MicAndSendButton(
                onPress = {arViewModel.sendButtonOnPress()},
                onRelease = {arViewModel.sendButtonOnRelease()},
                icon = painterResource(arViewModel.getMicOrSendIcon())
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInput(
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    onSend: () -> Unit,
    placeHolderText: @Composable (() -> Unit),
    colors: TextFieldColors,
    modifier: Modifier,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean
) {
    var previousFocusState by remember { mutableStateOf(false) }
    TextField(
        value = textFieldValue,
        onValueChange = { onTextChanged(it) },
        placeholder = placeHolderText,
        colors = colors,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            autoCorrect = true,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSend() }
        ),
        modifier = modifier.onFocusChanged { state ->
            if (previousFocusState != state.isFocused) {
                onTextFieldFocused(state.isFocused)
            }
            previousFocusState = state.isFocused
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MicAndSendButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    icon: Painter,
    description: String = "",
) {
    val pressed = remember { mutableStateOf(false) }

    val buttonColor by rememberUpdatedState(
        if (pressed.value) Color.DarkGray else Color.Transparent
    )

    val iconTint by rememberUpdatedState(
        if( pressed.value) Color.White else Color.Black
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(all = 8.dp)
            .background(color = buttonColor, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            onPress()
                            pressed.value = true
                            awaitRelease()
                        } finally {
                            onRelease()
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

@Composable
fun FloatingActionMenu(arViewModel: ArViewModel) {
    Column {
        ActionButton(
            onClick = { arViewModel.anchorOrFollowButtonOnClick() },
            color = Color.LightGray,
            values = arViewModel.getActionButtonValues(AvatarButtonType.MODE),
            enabled = arViewModel.enableActionButton(AvatarButtonType.MODE)
        )
        ActionButton(
            onClick = { arViewModel.summonOrHideButtonOnClick() },
            color = Color.LightGray,
            values = arViewModel.getActionButtonValues(AvatarButtonType.VISIBILITY),
            enabled = arViewModel.enableActionButton(AvatarButtonType.VISIBILITY)
        )
        ActionButton(
            onClick = { arViewModel.avatarButtonOnClick() },
            color = Color.Gray
        )
    }
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color,
    contentDescription: String = "",
    values: Pair<Int, String> = Pair(R.drawable.robot_face, "")
) {
    val interactionSource = remember { MutableInteractionSource() }

    val buttonColor by rememberUpdatedState(
        if (interactionSource.collectIsPressedAsState().value) Color.DarkGray else color
    )
    val iconTint by rememberUpdatedState(
        if (interactionSource.collectIsPressedAsState().value) Color.White else Color.Black
    )

    if (enabled) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                values.second,
                Modifier.padding(start = 8.dp)
            )
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .padding(5.dp)
                    .size(56.dp),
                shape = CircleShape,
                containerColor = buttonColor,
                interactionSource = interactionSource
            ) {
                Icon(
                    painter = painterResource(id = values.first),
                    contentDescription = "",
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    tint = iconTint
                )
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
fun ArScreenPreview() {
    ARAppTheme {
        ArScreen()
    }
}
