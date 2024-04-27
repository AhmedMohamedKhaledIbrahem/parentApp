package com.example.smartparent.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparent.data.viewmodel.ViewModelPhoto
import com.example.smartparent.theme.Black
import com.example.smartparent.theme.BlueGray


fun getCustomTextFiledInstance(): CustomTextFieldCompose {
    return CustomTextFieldCompose()
}

fun getCustomTextInstance(): CustomTextCompose {
    return CustomTextCompose()
}

fun getCustomButtonInstance(): CustomButtonCompose {
    return CustomButtonCompose()
}

fun getCustomSwitchCompose(): CustomSwitchCompose {
    return CustomSwitchCompose()
}


@Composable
fun CheckUiColorMode(): Color {
    return if (isSystemInDarkTheme()) BlueGray else Black
}

@Composable
fun CheckUiColorMode2(): Color {
    return if (isSystemInDarkTheme()) Color.White else Black
}


@Composable
fun getViewModelPhotoInstance(): ViewModelPhoto {
    return viewModel<ViewModelPhoto>()
}


fun getCustomImageInstance(): CustomImageCompose {
    return CustomImageCompose()
}



