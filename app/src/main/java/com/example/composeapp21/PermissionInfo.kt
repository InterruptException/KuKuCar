package com.example.composeapp21

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow

data class PermissionInfo(
    val permissionId: String,
    val isRequired: Boolean,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    @StringRes val reasonResId: Int,
    val isGranted: Boolean = false,
    val shouldShowRational: Boolean = false
)