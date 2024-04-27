package com.example.smartparent.ui.design


import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.smartparent.R
import com.example.smartparent.auth.GoogleAuthUiClient
import com.example.smartparent.data.BlindDataCache
import com.example.smartparent.data.EmailCache
import com.example.smartparent.data.TypeIdUserLogin
import com.example.smartparent.data.model.UserDataFromGoogleAccount
import com.example.smartparent.data.model.UserProfileModel
import com.example.smartparent.data.viewmodel.ConnectedDeviceContactViewModel
import com.example.smartparent.data.viewmodel.FirebaseViewModel
import com.example.smartparent.data.viewmodel.UserProfileViewModel
import com.example.smartparent.theme.skyBlue
import kotlin.system.exitProcess


class HomeView(private val context: Context, private val appCompatActivity: ComponentActivity) {
    private val textCompose: CustomTextCompose = getCustomTextInstance()
    private val imageCompose: CustomImageCompose = getCustomImageInstance()
    private var parse: String? = null


    @Composable
    fun homeView(
        navController: NavController, activity: ComponentActivity, vm: FirebaseViewModel,
        googleAuthUiClient: GoogleAuthUiClient,
        userDataFromGoogleAccount: UserDataFromGoogleAccount?,
    ) {


        Surface {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                LogoAndShapeAndTextIntroductionSection()
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp)
                ) {

                    ImageProfileAndFullNameSection(
                        userDataFromGoogleAccount,
                        vm,
                        googleAuthUiClient,
                    )
                    Spacer(modifier = Modifier.height(120.dp))
                    UserHomeSection(navController, vm, googleAuthUiClient)
                }
                YourDestinationScreen(navController, activity)

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
                ).CustomizeText(
                    text = stringResource(id = R.string.home),
                    modifier = Modifier.padding(vertical = 80.dp),
                )

            }


        }
    }

    @Composable
    fun ImageProfileAndFullNameSection(
        userDataFromGoogleAccount: UserDataFromGoogleAccount?,
        vm: FirebaseViewModel,
        googleAuthUiClient: GoogleAuthUiClient,

        ) {


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


        var selectedImage by remember {
            mutableStateOf(userProfileModelMutable.uri)
        }


        val pickPhoto =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    parse = getImagePathFromUri(context, uri)
                    userProfileModelMutable.uri = parse ?: ""
                    selectedImage = userProfileModelMutable.uri
                    userProfileViewModel.insertUserProfile(userProfileModelMutable)
                    vm.uploadImage(userProfileModelMutable.email, selectedImage.toString())
                }


            }




        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.height(100.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = skyBlue
                ),
            ) {
                Spacer(modifier = Modifier.height(25.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(32.dp))
                    val shape = RoundedCornerShape(
                        topStart = 50.dp,
                        topEnd = 50.dp,
                        bottomStart = 50.dp,
                        bottomEnd = 50.dp
                    )
                    if (userProfileModelMutable.uri.isNotEmpty()) {
                        selectedImage = userProfileModelMutable.uri
                        val bitmap by rememberUpdatedState(newValue = selectedImage)
                        val image = BitmapFactory.decodeFile(bitmap)
                        Image(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(shape = shape)
                                .clickable { pickPhoto.launch("image/*") },
                            bitmap = image.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Image(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(shape = shape)
                                .clickable { pickPhoto.launch("image/*") },
                            painter = painterResource(id = R.drawable.account_profile),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (userProfileModelMutable.fullName.isNotEmpty()) {
                        textCompose.copy(
                            fontSize = 18,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ).CustomizeTextImage(text = userProfileModelMutable.fullName)
                    }

                }
            }
        }
    }

    @Composable
    fun UserHomeSection(
        navController: NavController,
        vm: FirebaseViewModel,
        googleAuthUiClient: GoogleAuthUiClient,
    ) {
        val logoutViewModel: UserProfileViewModel by lazy {
            ViewModelProvider(appCompatActivity)[UserProfileViewModel::class.java]
        }
        val deleteConnectedDeviceContact :ConnectedDeviceContactViewModel by lazy {
            ViewModelProvider(appCompatActivity)[ConnectedDeviceContactViewModel::class.java]
        }

        Spacer(modifier = Modifier.height(8.dp))
        imageCompose.copy(
            modifierSizeImage = 80,
            fontSizeTextImage = 15,
            fontWeightTextImage = FontWeight.Bold,
        ).CustomizeImage(
            icon = R.drawable.connectedlogo,
            text = stringResource(id = R.string.connected)
        ) {
            navController.navigate("/connected")

        }
        Spacer(modifier = Modifier.height(8.dp))
        imageCompose.copy(
            modifierSizeImage = 80,
            fontSizeTextImage = 15,
            fontWeightTextImage = FontWeight.Bold,
        ).CustomizeImage(
            icon = R.drawable.logoutlogo,
            text = stringResource(id = R.string.LogoutLogo)
        ) {
            googleAuthUiClient.signOut()
            logoutViewModel.deleteAll()
            deleteConnectedDeviceContact.deleteAllConnectedDeviceContact()
            vm.signOut()
            EmailCache.emailCache = ""
            BlindDataCache.blindEmailCache = ""
            BlindDataCache.blindIdCache = ""

            TypeIdUserLogin.typeIdUserLogin = null
            navController.navigate("/login")

        }


    }


    private fun getImagePathFromUri(context: Context, uri: Uri): String? {
        var path: String? = null
        //val context = context
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            path = it.getString(columnIndex)
        }
        cursor?.close()
        return path
    }


    @Composable
    fun YourDestinationScreen(navController: NavController, activity: ComponentActivity) {
        BackHandler {
            navController.popBackStack()
            exitApp()
        }

    }


    /*
    //
    @RequiresApi(Build.VERSION_CODES.O)

     */
    /*
     val id = googleAuthUiClient.getSignedInUser()?.userId
        val email = googleAuthUiClient.getSignedInUser()?.email
        if (id != null) {
            if (email != null) {
                vm.getImageFromGoogleEmail(email, id)
            }
        }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun SettingSection(
        scaffoldState: BottomSheetScaffoldState,
        bitmaps: List<Bitmap>,
        controller: LifecycleCameraController,
        navController: NavController,

        ) {

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContent = {}

        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopEnd
            ) {
                CameraPreview(
                    controller,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        navController.navigate("/setting")
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.baseline_settings_24
                        ),
                        contentDescription = stringResource(R.string.SettingsIcon),
                        tint = Color.White
                    )
                }

            }


        }
    }



    @Composable
    private fun CameraPreview(
        controller: LifecycleCameraController,
        modifier: Modifier = Modifier
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = modifier
        )

    }


    private fun takePhoto(

        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit, mode: String
    ) {
        var photo: File? = null

        val photoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "${System.currentTimeMillis()}.jpg"
        )
        controller.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true,
                    )

                    photo = savePhotoToGallery(rotatedBitmap)
                    image.close()
                    uploadPhoto(photo!!, mode).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ data ->
                            tts?.speak(data[0].result, TextToSpeech.QUEUE_FLUSH, null, null)
                        }, { error ->

                        })
                    //Toast.makeText(context,"${photo?.absoluteFile}",Toast.LENGTH_SHORT).show()

                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )

    }


    @SuppressLint("SuspiciousIndentation")
    private fun savePhotoToGallery(bitmap: Bitmap): File? {
        val filename = "${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream?
        var image: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API to save the photo to the gallery
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                image = File(getImagePathFromUri(imageUri))

            }

            outputStream = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            // Save the photo to the Pictures directory
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            image = File(imagesDir, filename)
            outputStream = FileOutputStream(image)
        }

        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
        }
        return image
    }

    fun uploadPhoto(photo: File, mode: String): Observable<ArrayList<UploadResponse>> {

        val requestFile: RequestBody =
            RequestBody.create(MediaType.get("multipart/from-data"), photo)
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", photo.name, requestFile)
        val selectMode = RequestBody.create(MediaType.get("text/plain"), mode)
        val call: Observable<ArrayList<UploadResponse>> =
            SmartGlassApi().uploadImage(body, selectMode)

        return call

    }

    private fun getImagePathFromUri(uri: Uri): String? {
        var path: String? = null
        val context: Context = context
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                path = cursor.getString(columnIndex)
            }

        }
        cursor?.close()
        return path
    }

     */


    private fun exitApp() {
        exitProcess(0)
    }

}