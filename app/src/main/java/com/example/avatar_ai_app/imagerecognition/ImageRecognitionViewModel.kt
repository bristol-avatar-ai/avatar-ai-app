package com.example.avatar_ai_app.imagerecognition

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.avatar_ai_cloud_storage.database.AppDatabase
import com.example.avatar_ai_cloud_storage.network.CloudStorageApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel class for Image Recognition which handles the image classification process.
 *
 * @param application The application instance to access the app context and other app-level functionalities.
 */
class ImageRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    // The image classifier instance responsible for the actual classification.
    private var classifier: ImageClassifier? = null

    // Filename for the machine learning model.
    private val FILENAME = "model.tflite"

    // Retaining application instance for later use.
    private val _application = application

    // Application's context, mainly used for accessing files and app-level resources.
    private val context
        get() = _application.applicationContext

    /** LiveData to notify observers if the model has been downloaded.
     */
    val hasDownloaded: MutableLiveData<Boolean> = MutableLiveData(null)

    /** LiveData to notify observers when the classifier is ready for predictions.
      */
    val isReady: MutableLiveData<Boolean> = MutableLiveData(false)

    /** LiveData to hold the result of the image classification.
     */
    val result: MutableLiveData<String?> = MutableLiveData(null)

    /**
     * Initialises the image classifier. This involves downloading the model and setting up the classifier.
     */
    suspend fun initialiseClassifier() {
        // Model storage location.
        val modelFile = File(context.filesDir, FILENAME)

        // Update model from cloud storage if necessary.
        val updateModel = CloudStorageApi.updateModel(modelFile)

        // Check if the model file exists locally.
        val fileExists = modelFile.exists()

        // If the model cannot be obtained, notify observers and return.
        if (!updateModel && !fileExists) {
            hasDownloaded.postValue(false)
            return
        }

        hasDownloaded.postValue(true)

        // Initialise the classifier with the model.
        classifier = ImageClassifier(context, modelFile.canonicalPath)
        if (classifier!!.initialise()) {
            isReady.postValue(true)
            Log.d("IMAGE", "Loaded model")
        }
    }

    /**
     * Classify the provided bitmap image.
     *
     * @param bitmap The image that needs to be classified.
     */
    suspend fun classifyImage(bitmap: Bitmap) = withContext(Dispatchers.Default) {
        val output = classifier?.getExhibitName(bitmap)
        withContext(Dispatchers.Main) {
            result.postValue(output)
            Log.d("IMAGE", "Output is: $output")
        }
    }

    /**
     * Lifecycle method to handle cleanup. Closes the model to free up resources.
     */
    override fun onCleared() {
        super.onCleared()
        classifier?.closeModel()
    }
}