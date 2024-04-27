package com.example.smartparent.ui.design

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.smartparent.R
import com.example.smartparent.data.BlindDataCache
import com.example.smartparent.data.EmailCache
import com.example.smartparent.data.model.ConnectedDeviceContactModel
import com.example.smartparent.data.model.UserProfileModel
import com.example.smartparent.data.viewmodel.ConnectedDeviceContactViewModel
import com.example.smartparent.data.viewmodel.FirebaseViewModel
import com.example.smartparent.data.viewmodel.UserProfileViewModel
import com.example.smartparent.ui.design.CustomButtonCompose
import com.example.smartparent.ui.design.CustomTextCompose
import com.example.smartparent.ui.design.CustomTextFieldCompose
import com.example.smartparent.ui.design.getCustomButtonInstance
import com.example.smartparent.ui.design.getCustomTextFiledInstance
import com.example.smartparent.ui.design.getCustomTextInstance

class ConnectedDeviceContactView(
    private val appCompatActivity: ComponentActivity ,
    private val context:Context) {

    private val textFieldCompose: CustomTextFieldCompose = getCustomTextFiledInstance()
    private val textCompose: CustomTextCompose = getCustomTextInstance()
    private val buttonCompose: CustomButtonCompose = getCustomButtonInstance()

    @Composable
    fun connectedDeviceContactView(navController: NavController , vm:FirebaseViewModel){
        Surface() {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                LogoAndShapeAndTextIntroductionSection()
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp),

                    ) {
                    AddContactSection(navController,vm,context)
                    //Spacer(modifier = Modifier.height(8.dp))
                    //EmergencyFloatingActionButtonSection(navController)

                }

            }

        }

    }

    @Composable
    fun LogoAndShapeAndTextIntroductionSection() {
        Box(contentAlignment = Alignment.TopCenter) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.35f),
                painter = painterResource(id = R.drawable.shape),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                textCompose.copy(
                    fontSize = 30,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                    .CustomizeText(
                        text = stringResource(id = R.string.connectedDeviceContact),
                        modifier = Modifier.padding(vertical = 80.dp),
                    )
            }
        }
    }

    @Composable
    fun AddContactSection(navController: NavController,vm: FirebaseViewModel , context: Context){
        val connectedDeviceContactViewModel :ConnectedDeviceContactViewModel by  lazy {
            ViewModelProvider(appCompatActivity)[ConnectedDeviceContactViewModel::class.java]
        }
        var connectedDeviceContactMutableList by remember {
            mutableStateOf<List<ConnectedDeviceContactModel>>(emptyList())
        }
        val connectedDeviceContactLiveData = connectedDeviceContactViewModel.getConnectedDeviceContact()

        val observerConnectedDeviceContactModel = Observer<List<ConnectedDeviceContactModel>>{
            newData ->
            connectedDeviceContactMutableList = newData
        }
        connectedDeviceContactLiveData.observe(appCompatActivity,observerConnectedDeviceContactModel)

        val userProfileViewModel: UserProfileViewModel by lazy {
            ViewModelProvider(appCompatActivity)[UserProfileViewModel::class.java]
        }

        var userProfileModelMutable by remember {
            mutableStateOf(value = UserProfileModel())
        }

        val userProfileLiveData = userProfileViewModel.getUserProfileSingleData()
        val observerUserProfileModel = Observer<UserProfileModel?> { newData ->
            if (newData != null) {
                userProfileModelMutable = newData
            }
        }
        userProfileLiveData.observe(appCompatActivity, observerUserProfileModel)


        var textEmailValue by remember {
            mutableStateOf("")
        }
        textFieldCompose.copy(shape = 8).CustomizeTextField(
            onValueChanges = {
                             textEmailValue = it
                             },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email,imeAction = ImeAction.Next),
            label = {
                textCompose.copy().CustomizeText(
                    text = stringResource(id = R.string.EmailLogin)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription =null ) }
        )
        Spacer(modifier = Modifier.height(20.dp))
        buttonCompose.copy(shape = 8).CustomizeButton(
            modifier = Modifier.fillMaxWidth(),
            onclick = {
                if (textEmailValue.isNotEmpty()) {
                    vm.connectedDeviceUser(textEmailValue){
                    val connectedDeviceContactModel = ConnectedDeviceContactModel(
                        blindEmail = BlindDataCache.blindEmailCache,
                        blindId = BlindDataCache.blindIdCache,
                        personEmail = userProfileModelMutable.email,
                        personId = userProfileModelMutable.emailID,
                    )


                    connectedDeviceContactViewModel.insertConnectedDeviceContact(
                        connectedDeviceContactModel
                    )
                    navController.navigate("/connected")
                }

                }

            },
        ) {
            textCompose.copy(fontSize = 18).CustomizeText(
                text = stringResource(R.string.AddContact)
            )
        }

    }

}