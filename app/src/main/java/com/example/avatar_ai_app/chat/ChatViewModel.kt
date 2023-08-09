package com.example.avatar_ai_app.chat

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avatar_ai_app.ErrorListener
import com.example.avatar_ai_app.audio.AudioRecorder
import com.example.avatar_ai_app.chat.ChatViewModelInterface.Status
import com.example.avatar_ai_app.language.ChatTranslator
import com.example.avatar_ai_app.language.Language
import com.example.avatar_ai_app.network.TranscriptionApi
import com.example.avatar_ai_app.shared.ErrorType
import com.example.avatar_ai_cloud_storage.database.Exhibition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ChatViewModel"

private const val RECORDING_NAME = "recording"
private const val RECORDING_FILE_TYPE = "ogg"
private const val TOTAL_INIT_COUNT = 2

/**
 * ViewModel containing the chat history and methods to modify it.
 */

class ChatViewModel(
    context: Context,
    private var language: Language,
    private val errorListener: ErrorListener
) : ViewModel(),
    ChatViewModelInterface,
    OnInitListener,
    AudioRecorder.RecordingCompletionListener,
    ChatTranslator.InitListener {

    // Save the recordings filepath to the cache directory.
    private val recordingFile: File =
        File.createTempFile(RECORDING_NAME, RECORDING_FILE_TYPE, context.cacheDir)

    private val _status = MutableLiveData(Status.INIT)
    override val status: LiveData<Status> get() = _status

    // Store message history as MutableLiveData backing property.
    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    override val messages: LiveData<MutableList<ChatMessage>> get() = _messages

    // Initialise ChatService for message responses.
    private val chatService = ChatService()

    override val request = chatService.request

    override val destinationID = chatService.destinationID

    // Initialise TextToSpeech class for audio responses.
    private val textToSpeech: TextToSpeech = TextToSpeech(context, this)

    private var textToSpeechReady = false

    // Initialise AudioRecorder class for recording audio input.
    private val audioRecorder: AudioRecorder =
        AudioRecorder(context, recordingFile, viewModelScope, this)

    private val chatTranslator = ChatTranslator(language.mlKitLanguage, this)

    private var initCount = 0

    /*
    * Override onCleared() to release initialised resources
    * when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        _messages.value?.clear()
        chatService.reset()
        audioRecorder.release()
        textToSpeech.stop()
        textToSpeech.shutdown()
        chatTranslator.close()
        initCount = 0
    }

    private fun componentInitialised() {
        initCount++
        if (initCount >= TOTAL_INIT_COUNT) {
            _status.postValue(Status.READY)
        }
    }

    override fun onTranslatorInit(success: Boolean) {
        componentInitialised()
    }

    /*
    * onInit is called when the TextToSpeech service is initialised.
    * Initialisation errors are handled and the language is set.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setTextToSpeechLanguage()
        } else {
            Log.e(TAG, "Failed to initialise TextToSpeech")
            errorListener.onError(ErrorType.NETWORK)
        }
    }

    /*
    * This function sets the TextToSpeech language and handles any errors.
     */
    private fun setTextToSpeechLanguage() {
        val result = textToSpeech.setLanguage(language.locale)

        textToSpeechReady = if (result == TextToSpeech.LANG_MISSING_DATA
            || result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            Log.e(TAG, "Failed to set TextToSpeech language")
            errorListener.onError(ErrorType.SPEECH)
            false
        } else {
            true
        }
        componentInitialised()
    }

    override fun setLanguage(language: Language) {
        this.language = language
        _status.value = Status.INIT
        initCount = 0
        setTextToSpeechLanguage()
        chatTranslator.setLanguage(language.mlKitLanguage)
    }

    override fun setExhibitionList(exhibitionList: List<Exhibition>) {
        chatService.exhibitionList = exhibitionList
    }

    /*
    * Adds a new message to the chat history.
    * Newest messages are stored first.
    */
    private fun addMessage(message: ChatMessage) {
        // Get the current message list, or create one if null.
        val currentMessages = _messages.value ?: mutableListOf()
        // Add the new message to the list
        currentMessages.add(0, message)
        // Set the updated list back to the MutableLiveData using postValue
        _messages.postValue(currentMessages)
    }

    /*
    * Processes user message input. Adds the new
    * user message, then generates a reply.
     */
    /*
    * Gets a response to the input message with ChatService. Generates an audio
    * reply and plays it if TextToSpeech has been initialised correctly.
    * Coroutines are used to prevent blocking the main thread.
     */
    override fun newUserMessage(message: String) {
        if (language == Language.ENGLISH) {
            addMessage(ChatMessage(message, ChatMessage.USER))
            viewModelScope.launch(Dispatchers.IO) {
                // Generate reply with ChatService.
                val response = chatService.getResponse(message)
                readMessage(response)
                addMessage(ChatMessage(response, ChatMessage.AI))
            }
        } else {
            newNonEnglishMessage(message)
        }
    }

    private fun newNonEnglishMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val englishMessage = chatTranslator.translateMessage(message)
            if (englishMessage == null) {
                errorListener.onError(ErrorType.NETWORK)
            } else {
                val englishResponse = chatService.getResponse(englishMessage)
                val response = chatTranslator.translateResponse(englishResponse)
                if (response == null) {
                    errorListener.onError(ErrorType.NETWORK)
                } else {
                    readMessage(response)
                    addMessage(ChatMessage(response, ChatMessage.AI))
                }
            }
        }
    }

    /*
    * Reads message with TextToSpeech if ready.
     */
    private fun readMessage(message: String) {
        if (textToSpeechReady) {
            textToSpeech.speak(
                message,
                TextToSpeech.QUEUE_FLUSH, null, null
            )
        }
    }

    /*
    * This function starts the AudioRecorder, then disables
    * text input and changes the hint.
     */
    override fun startRecording() {
        try {
            audioRecorder.start()
            _status.postValue(Status.RECORDING)
        } catch (_: Exception) {
            errorListener.onError(ErrorType.RECORDING)
        }
    }

    override fun stopRecording() {
        audioRecorder.stop()
    }

    /*
    * onRecordingCompleted is called when the AudioRecorder stops recording.
    * The action button is disables and the hint is modified. A reply is
    * generated before the control and hint are reset.
     */
    /*
    * This function transcribes the recordingFile into text, deletes the
    * file, and then generates a reply.
     */
    override fun onRecordingCompleted() {
        _status.postValue(Status.PROCESSING)
        viewModelScope.launch {
            val message = TranscriptionApi.transcribe(recordingFile, language.ibmModel)
            if (recordingFile.exists()) {
                recordingFile.delete()
            }
            if (message != null) {
                newUserMessage(message)
            } else {
                errorListener.onError(ErrorType.NETWORK)
            }
            _status.postValue(Status.READY)
        }
    }

    override fun newResponse(response: String) {
        viewModelScope.launch(Dispatchers.IO) {
            readMessage(response)
            addMessage(ChatMessage(response, ChatMessage.AI))
        }
    }

    /*
    * Clear the message history.
    */
    override fun clearChatHistory() {
        _messages.value?.clear()
    }

}