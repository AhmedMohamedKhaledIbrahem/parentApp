package com.example.smartparent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartparent.auth.GoogleAuthUiClient
import com.example.smartparent.data.viewmodel.FirebaseViewModel
import com.example.smartparent.data.viewmodel.NetworkConnectivityViewModel
import com.example.smartparent.data.viewmodel.SignInWithGoogleStateViewModel
import com.example.smartparent.service.VoiceRecognitionService
import com.example.smartparent.theme.SmartGlassTheme
import com.example.smartparent.ui.design.ConnectedDeviceContactView
import com.example.smartparent.ui.design.ConnectedDeviceView
import com.example.smartparent.ui.design.ForgetPasswordView
import com.example.smartparent.ui.design.HomeView
import com.example.smartparent.ui.design.LoginView
import com.example.smartparent.ui.design.PasswordChangeView
import com.example.smartparent.ui.design.SignUpView
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var networkConnectivityViewModel: NetworkConnectivityViewModel
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private var tts: TextToSpeech? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginView = LoginView(this, this)
        val signUpView = SignUpView(this, this)
        val homeView = HomeView(applicationContext, this@MainActivity)
        val connectedDeviceView = ConnectedDeviceView(this)
        val connectedDeviceContactView = ConnectedDeviceContactView(this, this)
        val forgetPasswordView = ForgetPasswordView(this)
        val passwordChangeView = PasswordChangeView()
        networkConnectivityViewModel = NetworkConnectivityViewModel(this)


        tts = TextToSpeech(this)
        { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.getDefault()
            }
        }

        if (!hasRequiredPermissionsCamera()) {
            ActivityCompat.requestPermissions(
                this, CAMERA_PERMISSIONS, 0
            )
        }
        networkConnectivityViewModel.observe(this) { isConnected ->
            if (isConnected) {

            } else {
                tts?.speak(
                    "lost connection ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                )

            }


        }

        setContent {
            SmartGlassTheme {

                val navController = rememberNavController()
                // Define the navigation graph
                NavigationControllerLoginViewToSignUpView(
                    navController,
                    loginView,
                    signUpView,
                    homeView,
                    forgetPasswordView,
                    passwordChangeView,
                    connectedDeviceView,
                    connectedDeviceContactView
                )


            }


        }


    }


    override fun onStop() {
        super.onStop()
        stopVoiceRecognitionService()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        stopVoiceRecognitionService()
    }

    private fun stopVoiceRecognitionService() {
        val serviceIntent = Intent(this, VoiceRecognitionService::class.java)
        stopService(serviceIntent)
    }

    @SuppressLint("RememberReturnType")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun NavigationControllerLoginViewToSignUpView(
        navController: NavHostController,
        loginView: LoginView,
        signUpView: SignUpView,
        homeView: HomeView,
        forgetPasswordView: ForgetPasswordView,
        passwordChangeView: PasswordChangeView,
        connectedDeviceView: ConnectedDeviceView,
        connectedDeviceContactView: ConnectedDeviceContactView,
    ) {


        val viewModelFireBase = hiltViewModel<FirebaseViewModel>()

        LaunchedEffect(Unit) {
            if (viewModelFireBase.isSignedIn()) {
                navController.navigate("/home")
            }
        }


        NavHost(navController = navController, startDestination = "/login") {
            composable("/login") {
                val viewModel = viewModel<SignInWithGoogleStateViewModel>()
                val state by viewModel.state.collectAsState()
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
                            lifecycleScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                viewModel.onSignInResult(signInResult)

                            }
                        }
                    }
                )

                LaunchedEffect(key1 = state) {

                    if (state.isSignInSuccessful) {
                        googleAuthUiClient.getSignedInUser()?.username?.let { it1 ->
                            viewModelFireBase.uploadEmailGoogleToDatabase(
                                googleAuthUiClient.getSignedInUser()!!.email,
                                it1,
                                googleAuthUiClient.getSignedInUser()!!.userId
                            )

                        }
                        val getIdType = viewModelFireBase.getIdTypeUserGoogleAccount(
                            googleAuthUiClient.getSignedInUser()!!.email,
                            googleAuthUiClient.getSignedInUser()!!.userId
                        )
                        if (getIdType == 1 || getIdType == null) {

                            viewModelFireBase.fetchUserProfileFromFirebase(
                                googleAuthUiClient.getSignedInUser()!!.email
                            )
                            Toast.makeText(
                                applicationContext,
                                "Sign In successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("/home")
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Sign In Unsuccessful",
                                Toast.LENGTH_SHORT
                            ).show()
                            googleAuthUiClient.signOut()
                            viewModelFireBase.signOut()
                            navController.navigate("/login")
                        }

                    }


                }
                loginView.loginView(navController = navController, viewModelFireBase, state = state,
                    onSignInClick = {
                        lifecycleScope.launch {
                            val signINIntentSender = googleAuthUiClient.signIn()

                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    signINIntentSender ?: return@launch
                                ).build()
                            )
                        }
                    })


            }
            composable("/signup") {
                signUpView.signUpView(navController = navController, viewModelFireBase)
            }
            composable(
                "/home",
                arguments = listOf(navArgument("popUpTo") { defaultValue = "/login" })
            ) {
                homeView.homeView(
                    navController = navController,
                    this@MainActivity,
                    viewModelFireBase,
                    googleAuthUiClient,
                    googleAuthUiClient.getSignedInUser(),
                )

            }


            composable("/forgetPasswordView") {
                forgetPasswordView.forgetPasswordView(
                    navController = navController,
                    viewModelFireBase
                )
            }
            composable("/passwordChange") {
                passwordChangeView.passwordChangeView(navController = navController)
            }
            composable("/connected") {
                connectedDeviceView.connectedDeviceView(navController = navController)
            }
            composable("/connectedContact") {
                connectedDeviceContactView.connectedDeviceContactView(
                    navController = navController,
                    viewModelFireBase
                )
            }

        }


    }


    private fun hasRequiredPermissionsCamera(): Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    }


    companion object {
        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY ,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }


}


