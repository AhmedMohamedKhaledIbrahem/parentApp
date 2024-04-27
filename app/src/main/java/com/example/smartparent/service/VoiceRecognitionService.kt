package com.example.smartparent.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.smartparent.data.viewmodel.VoiceCommandViewModel
import java.util.Locale

class VoiceRecognitionService : Service() {


    private var recognizerIntent: Intent? = null
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognition", "Ready for speech")

            }

            override fun onBeginningOfSpeech() {
                Log.i("onBeginningOfSpeech", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.i("onRmsChanged", "onRmsChanged")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                TODO("Not yet implemented")
            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {
                Log.i("onError", "onError ")
                startListening()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { command ->
                    if (command.lowercase().contains("detect")) {
                        val viewModel = VoiceCommandViewModel
                        viewModel.updateData(matches[0]!!)

                        Toast.makeText(
                            this@VoiceRecognitionService,
                            "Detect",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (command.lowercase().contains("money")) {
                        val viewModel = VoiceCommandViewModel
                        viewModel.updateData(matches[0]!!)

                        Toast.makeText(
                            this@VoiceRecognitionService,
                            "money",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (command.lowercase().contains("describe")) {
                        val viewModel = VoiceCommandViewModel
                        viewModel.updateData(matches[0]!!)

                        Toast.makeText(
                            this@VoiceRecognitionService,
                            "describe",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (command.lowercase().contains("read")) {
                        val viewModel = VoiceCommandViewModel
                        viewModel.updateData(matches[0]!!)

                        Toast.makeText(
                            this@VoiceRecognitionService,
                            "text",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.i("onRESULTS", "onResults $command")
                    }
                }
                startListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.i("onPartialResults", "onPartialResults $")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }
        })
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(FOREGROUND_SERVICE_ID, notification)
        startListening()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }


    private fun startListening() {
        if (recognizerIntent == null) {
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

        }

        speechRecognizer.startListening(recognizerIntent)
    }

    fun onStop() {
       stopService(recognizerIntent)

      }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "VoiceRecognitionServiceChannel"
        val channelName = "Voice Recognition Service Channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Listening for voice commands")
            .setContentText("Listening for voice commands")
            .build()
    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 101
    }

}

