package com.example.smartparent.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartparent.data.model.GoogleSignInResult
import com.example.smartparent.data.model.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInWithGoogleStateViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
    fun onSignInResult(result: GoogleSignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}