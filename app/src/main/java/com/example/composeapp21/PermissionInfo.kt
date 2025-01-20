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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionInfo

        if (permissionId != other.permissionId) return false
        if (isRequired != other.isRequired) return false
        if (nameResId != other.nameResId) return false
        if (descriptionResId != other.descriptionResId) return false
        if (reasonResId != other.reasonResId) return false
        if (isGranted != other.isGranted) return false
        if (shouldShowRational != other.shouldShowRational) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissionId.hashCode()
        result = 31 * result + isRequired.hashCode()
        result = 31 * result + nameResId
        result = 31 * result + descriptionResId
        result = 31 * result + reasonResId
        result = 31 * result + isGranted.hashCode()
        result = 31 * result + shouldShowRational.hashCode()
        return result
    }

    override fun toString(): String {
        return "PermissionInfo(permissionId='$permissionId', isRequired=$isRequired, isGranted=$isGranted, shouldShowRational=$shouldShowRational)"
    }


}