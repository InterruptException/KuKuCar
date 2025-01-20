package com.example.composeapp21

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel

class WelcomeViewModel: ViewModel() {
    val completedPageIndex = mutableIntStateOf(0)
    private val permissionGroups = mutableStateListOf<PermissionGroupInfo>()

    fun initViewModel(context: Context) {
        permissionGroups.add(PermissionGroupInfo(
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
        ))
        permissionGroups.add(
            PermissionGroupInfo(
                listOf(
                    PermissionInfo(
                        permissionId = Manifest.permission.LOCATION_HARDWARE,
                        isRequired = false,
                        nameResId = R.string.perm_name_location_hardware,
                        descriptionResId = R.string.perm_desc_location_hardware,
                        reasonResId = R.string.perm_reason_location_hardware
                    )
                )
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionGroups.add(
                PermissionGroupInfo(
                    listOf(
                        PermissionInfo(
                            permissionId = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            isRequired = false,
                            nameResId = R.string.perm_name_access_bg_location,
                            descriptionResId = R.string.perm_desc_access_bg_location,
                            reasonResId = R.string.perm_reason_access_bg_location
                        )
                    )
                )
            )
        }
    }

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
        Log.i("WelcomeViewModel", "更新权限状态")
    }

    private val permReqMap = mutableStateMapOf<Int, PermissionRequest>()

    fun addPermissionRequest(req: PermissionRequest) {
        permReqMap.put(req.pageIndex, req)
    }

    fun findDeniedPermissions(): List<PermissionGroupInfo> {
        return permissionGroups.filter { permGroup -> permGroup.permissionStates.any { !it.isGranted && it.isRequired } }
    }

    fun updatePermissionState(result: Map<String, Boolean>) {
        val reqPerm = permReqMap.values.find { req ->
            req.permissionGroupInfo.permissionStates.all { it.permissionId in result.keys }
        }
        if (reqPerm == null) {
            val resultJson = result.entries.joinToString(prefix = "{ ", postfix = " }") { entry->
                "'${entry.key}' : ${entry.value}"
            }
            Log.w("权限请求", "找不到匹配的请求组 $resultJson")
            return
        }

        val i = permissionGroups.indexOf(reqPerm.permissionGroupInfo)
        if (i == -1) {
            Log.w("权限请求", "找不到匹配的权限列表项")
            return
        }

        val newPermGroupInfo = reqPerm.permissionGroupInfo.let {
            it.copy(permissionStates = it.permissionStates.map { p->
                p.copy(isGranted = result[p.permissionId] ?: false)
            })
        }
        permissionGroups[i] = newPermGroupInfo
        Log.i("权限请求", "更新权限列表项")
        if (newPermGroupInfo.permissionStates.filter { it.isRequired }.all { it.isGranted }) {
            if (completedPageIndex.intValue < reqPerm.pageIndex) {
                completedPageIndex.intValue = reqPerm.pageIndex
                Log.i("权限请求", "更新完成的页面索引")
            }
        } else {
            val deniedPermInfo = newPermGroupInfo.permissionStates.filter { it.isRequired && !it.isGranted }.joinToString(prefix = "[ ", postfix = " ]") {
                """
                    |{
                    |   "permissionId": "${it.permissionId}",
                    |   "isGrant": ${it.isGranted}
                    |}
                """.trimMargin("|")
            }
            Log.w("权限请求", "部分权限未完成授权：$deniedPermInfo")
        }
    }

    fun completeStep(currentStep: Int) {
        completedPageIndex.intValue = currentStep+1
    }
}

data class PermissionRequest (
    val pageIndex: Int,
    val permissionGroupInfo: PermissionGroupInfo
)