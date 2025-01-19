package com.example.composeapp21

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel

class WelcomeViewModel: ViewModel() {
    val completedPageIndex = mutableIntStateOf(0)
    private val permissionGroups = mutableStateListOf<PermissionGroupInfo>(
            PermissionGroupInfo(
                listOf(
                    PermissionInfo(
                        permissionId = Manifest.permission.ACCESS_FINE_LOCATION,
                        isRequired = true,
                        nameResId = R.string.perm_name_coarse_location,
                        descriptionResId = R.string.perm_desc_coarse_location,
                        reasonResId = R.string.perm_reason_coarse_location
                    ),
                    PermissionInfo(
                        permissionId = Manifest.permission.ACCESS_COARSE_LOCATION,
                        isRequired = true,
                        nameResId = R.string.perm_name_fine_location,
                        descriptionResId = R.string.perm_desc_fine_location,
                        reasonResId = R.string.perm_reason_fine_location
                    )
                )
            )
        )

    fun updatePermissionStates(activity: Activity) {
        val newPermGroups = permissionGroups.map { permGroup->
            permGroup.copy(
                permGroup.permissionStates.map { permissionState ->
                    val isGranted = ContextCompat.checkSelfPermission(
                        activity,
                        permissionState.permissionId
                    ) == PackageManager.PERMISSION_GRANTED
                    val shouldShowRational = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionState.permissionId)
                    permissionState.copy(isGranted = isGranted, shouldShowRational = shouldShowRational)
                }
            )
        }
        permissionGroups.clear()
        permissionGroups.addAll(newPermGroups)
    }

    fun findDeniedPermissions(): List<PermissionGroupInfo> {
        return permissionGroups.filter { permGroup -> permGroup.permissionStates.any { !it.isGranted && it.isRequired } }
    }

    fun updatePermissionState(result: Map<String, Boolean>) {

    }

    fun completeStep(currentStep: Int) {
        completedPageIndex.intValue = currentStep+1
    }
}