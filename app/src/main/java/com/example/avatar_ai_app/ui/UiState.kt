package com.example.avatar_ai_app.ui

import androidx.compose.runtime.mutableStateListOf
import com.example.avatar_ai_app.R
import com.example.avatar_ai_app.chat.ChatMessage
import com.example.avatar_ai_app.language.Language


data class UiState(
    //UI
    val isLoaded: Boolean = false,
    val inputMode: Int = speech,
    val textFieldStringResId: Int = R.string.send_message_hint,

    //Text input
    val isTextToSpeechReady: Boolean = false,

    //Language
    val language: Language = Language.ENGLISH,
    val isLanguageMenuShown: Boolean = false,

    //Alert message
    val alertIsShown: Boolean = false,
    val alertResId: Int = R.string.empty_string,
    val alertIntent: Int = help,

    //Messages
    val messages: MutableList<ChatMessage> = mutableStateListOf(),
    val messagesAreShown: Boolean = false,

    //Settings menu
    val isSettingsMenuShown: Boolean = false

) {
    companion object {
        const val text = 0
        const val speech = 1
        const val help = 0
        const val clear = 1
    }

    fun addMessage(msg: ChatMessage) {
        messages.add(0, msg)
    }

    fun clearMessages() {
        messages.clear()
    }
}