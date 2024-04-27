package com.example.smartparent.data.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.smartparent.data.BlindDataCache
import com.example.smartparent.data.TypeIdUserLogin
import com.example.smartparent.data.model.ConnectedDeviceContactModel

import com.example.smartparent.data.model.UserProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userProfileViewModel: UserProfileViewModel,
    private val connectedDeviceContactViewModel: ConnectedDeviceContactViewModel
) : ViewModel() {


    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val dataReferenceUsers: DatabaseReference = database.getReference("users")
    private val dataReferenceConnectedUsers: DatabaseReference =
        database.getReference("connectedUsers")
    val signedIn = mutableStateOf(false)
    private val inProcess = mutableStateOf(false)
    val popupNotification = mutableStateOf(null)
    val isRestPassword = mutableStateOf(false)
     lateinit var  blindId :String
     lateinit var  blindEmail:String
    fun onSignup(
        user: UserProfileModel,
        email: String,
        password: String,
        callback: (Boolean) -> Unit
    ) {
        val encodeEmail = user.email.replace(".", "_").replace("@", "_")

        inProcess.value = true
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = it.result?.user
                val uid = firebaseUser?.uid
                val userprofileMap = mapOf(
                    "fullName" to user.fullName,
                    "userName" to user.userName,
                    "email" to user.email,
                    "emailID" to uid,
                    "phoneNumber" to user.phoneNumber,
                    "password" to user.password,
                    "typeId" to user.typeId,
                    "uri" to user.uri
                )
                callback(true)
                dataReferenceUsers.child(encodeEmail).setValue(userprofileMap)
                signedIn.value = true
            } else {
                callback(false)
                Log.e("signUp", "the email already used")
            }
            inProcess.value = false
        }
    }

    fun login(email: String, password: String, context: Context, callback: (Boolean) -> Unit) {
        inProcess.value = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signedIn.value = true
                    callback(true)
                    Toast.makeText(context, "The Login successful", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "success to log in: $auth")
                } else {
                    Log.e("LoginFailed", "Failed to log in: $auth")
                    callback(false)
                    Toast.makeText(
                        context,
                        "The Login Failed Check Email or Password ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                inProcess.value = false
            }
    }

    fun uploadImage(email: String, uri: String) {
        val encodeEmail = email.replace(".", "_").replace("@", "_")
        dataReferenceUsers.child(encodeEmail).child("uri").setValue(uri)
    }

    fun uploadEmailGoogleToDatabase(emailGoogle: String, name: String, idGoogle: String) {
        val encodeEmail = emailGoogle.replace(".", "_").replace("@", "_")

        val userRef = dataReferenceUsers.child(encodeEmail)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    val user = mapOf(
                        "email" to emailGoogle,
                        "fullName" to name,
                        "emailID" to idGoogle,
                        "typeId" to 1,
                        "uri" to "",
                    )
                    userRef.setValue(user)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun connectedDeviceUser(
        email: String,
        onComplete:() -> Unit
    ) {
        val encodeEmail = email.replace(".", "_").replace("@", "_")
        val user = FirebaseAuth.getInstance().currentUser
        val personId = user?.uid
        val personEmail = user?.email
        dataReferenceUsers.child(encodeEmail).child("email").get()
            .addOnSuccessListener { emailSnapshot ->
                 blindEmail = emailSnapshot.value.toString()
                dataReferenceUsers.child(encodeEmail).child("emailID").get()
                    .addOnSuccessListener { idSnapshot ->
                         blindId = idSnapshot.value.toString()

                        val connectedDeviceUserMapOf = mapOf(
                            "personEmail" to personEmail,
                            "personId" to personId,
                            "blindEmail" to blindEmail,
                            "blindId" to blindId,
                            "connected" to 0
                        )

                        val uniqueKey = "${personId}${blindId.take(4)}"

                        // Save the data to the database
                        dataReferenceConnectedUsers.child("connectedUser").child(uniqueKey).setValue(connectedDeviceUserMapOf)
                            .addOnSuccessListener {
                                BlindDataCache.blindIdCache = blindId
                                BlindDataCache.blindEmailCache = blindEmail
                                onComplete()

                                Log.i("Data saved successfully","Data saved successfully")
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure to save data
                                println("Failed to save data: ${exception.message}")
                                Log.i("Data not successfully","Data not successfully")
                            }


                    }
            }
    }


    fun getTypeIdUserLogin(email: String) {

        val encodeEmail = email.replace(".", "_").replace("@", "_")
        if (encodeEmail.isNotEmpty()) {

            dataReferenceUsers.child(encodeEmail).child("typeId")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userData = dataSnapshot.getValue(Int::class.java)
                        if (userData != null) {
                            TypeIdUserLogin.typeIdUserLogin = userData
                        }
                    }


                    override fun onCancelled(dataSnapshot: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }


    }

    suspend fun getIdTypeUserGoogleAccount(
        googleEmail: String,
        googleEmailId: String,
    ): Int? {
        val encodeEmail = googleEmail.replace(".", "_").replace("@", "_")
        val currentUserId = FirebaseAuth.getInstance().currentUser
        val idGoogle = currentUserId?.uid
        if (googleEmailId == idGoogle) {
            val dataSnapshot =
                dataReferenceUsers.child(encodeEmail).child("typeId").get()
                    .await()
            return dataSnapshot.getValue(Int::class.java)
        }
        return null

    }


    fun fetchUserProfileFromFirebase(email: String) {
        val encodeEmail = email.replace(".", "_").replace("@", "_")
        dataReferenceUsers.child(encodeEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfileData =
                        snapshot.getValue(UserProfileModel::class.java)

                    if (userProfileData != null) {
                        // Save user profile data to local Room database
                        userProfileViewModel.insertUserProfile(userProfileData)

                    } else {
                        Log.e(
                            "Firebase",
                            "User profile data not found in Firebase database"
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "Firebase",
                        "Failed to fetch user profile data: ${error.message}"
                    )
                }
            })
    }

    fun isEmailRegistered(
        email: String,
        context: Context,
        onSuccess: (Boolean) -> Unit
    ) {
        val encodeEmail = email.replace(".", "_").replace("@", "_")
        dataReferenceUsers.child(encodeEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onSuccess(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "Firebase",
                        "Your Email not registered  : ${error.message}"
                    )
                    Toast.makeText(
                        context,
                        "Your Email not registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit) {

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                isRestPassword.value = false
            }
    }

    fun updatedPasswordInDatabase(email: String, newPassword: String) {
        val encodeEmail = email.replace(".", "_").replace("@", "_")
        val userReference = database.getReference("users").child(encodeEmail)

        userReference.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userReference.ref.child("password").setValue(newPassword)
                }
            }

            override fun onCancelled(snapshot: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }


    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }





}
