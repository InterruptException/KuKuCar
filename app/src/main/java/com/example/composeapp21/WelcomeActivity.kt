package com.example.composeapp21

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composeapp21.ui.theme.ComposeApp21Theme

class WelcomeActivity : ComponentActivity() {
    val vm by viewModels<WelcomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vm.initViewModel(this)
        vm.updatePermissionStates(this)
        val deniedPerms = vm.findDeniedPermissions()
        if (deniedPerms.none()) {
            enterMainActivity()
        } else {
            val permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                vm.updatePermissionState(result)
            }
            setContent {
                ComposeApp21Theme {
                    PermissionScreen(
                        permGroups = deniedPerms,
                        onRequestPermission = { req->
                            vm.addPermissionRequest(req)
                            permReqLauncher.launch(req.permissionGroupInfo.permissionStates.map { it.permissionId }.toTypedArray())
                        },
                        onComplete = {
                            enterMainActivity()
                        },
                        completedPageNumber = {
                            vm.completedPageIndex
                        }
                    )
                }
            }
        }
    }

    private fun enterMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun PermissionScreen(
                     permGroups: List<PermissionGroupInfo>,
                     onRequestPermission: (request: PermissionRequest)->Unit = {  },
                     onComplete: ()->Unit = {},
                     completedPageNumber: ()->MutableIntState = { mutableIntStateOf(0) }
) {
    val pageCount = remember(permGroups) {
        permGroups.size + 2 // 第一页+若干权限组申请页+最后一页
    }
    val pagerState = rememberPagerState {
        pageCount
    }
    val allowPageIndex = remember(completedPageNumber) {
        completedPageNumber()
    }

    Scaffold(modifier = Modifier
        .windowInsetsPadding(WindowInsets.systemBars)
        .fillMaxSize(),
        bottomBar = {
            NavigationButtons(
                modifier = Modifier.fillMaxWidth(),
                onClickPrevious = if (pagerState.currentPage == 0) null else {
                    {
                        pagerState.requestScrollToPage(pagerState.currentPage - 1)
                    }
                },

                onClickNext = {
                    if (pagerState.currentPage == pageCount - 1) {
                        onComplete()
                    } else if (allowPageIndex.intValue >= pagerState.currentPage) {
                        pagerState.requestScrollToPage(pagerState.currentPage + 1)
                    }
                },
                allowNext = allowPageIndex.intValue >= pagerState.currentPage,
                nextButtonText = if (pagerState.canScrollForward) R.string.next_step else R.string.enter_main_page
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { pageIndex->
                if (pagerState.currentPage == 0) {
                    FirstPage()
                } else if (pagerState.currentPage == pageCount - 1) {
                    LastPage(
                        onEnter = {
                            allowPageIndex.intValue = pageCount - 1
                        }
                    )
                } else {
                    MiddlePage(
                        currentPageIndex = pageIndex,
                        firstPageIndex = 1,
                        lastPageIndex = pageCount - 2,
                        permGroups = permGroups,
                        onClickRequest = onRequestPermission
                    )
                }
            }
        }
    }
}

@Composable
fun FirstPage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.ic_speed), contentDescription = "logo", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(0.5f))
    }
}

@Composable
fun NavigationButtons(
    modifier: Modifier = Modifier,
    onClickPrevious: (() -> Unit)? = null,
    onClickNext: (() -> Unit)? = null,
    allowNext: Boolean = false,
    @StringRes nextButtonText: Int = R.string.enter_main_page
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (onClickPrevious != null) {
            Button(onClick = onClickPrevious) {
                Text(stringResource(R.string.last_step))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        if (onClickNext != null) {
            Button(onClick = onClickNext, enabled = allowNext) {
                Text(stringResource(nextButtonText))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}



@Composable
fun MiddlePage(
    currentPageIndex: Int,
    firstPageIndex: Int,
    lastPageIndex: Int,
    permGroups: List<PermissionGroupInfo>,
    onClickRequest: (PermissionRequest)->Unit = { },
) {
    val currentDataItemIndex = remember(currentPageIndex, firstPageIndex, lastPageIndex) {
        currentPageIndex - firstPageIndex
    }
    if (currentPageIndex in firstPageIndex .. lastPageIndex && currentDataItemIndex <= permGroups.lastIndex){

        val permGroup = remember(currentPageIndex, firstPageIndex, lastPageIndex, permGroups) {
            permGroups[currentDataItemIndex]
        }

        Column ( modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("权限请求页面")
            permGroup.permissionStates.forEachIndexed { index, perm ->
                Text("权限名：${stringResource(perm.nameResId)}")
                Text("权限描述：${stringResource(perm.descriptionResId)}")
                Text("请求理由：${stringResource(perm.reasonResId)}")
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(onClick = {
                onClickRequest(PermissionRequest(currentPageIndex, permGroup))
            }) {
                Text(stringResource(R.string.acquire_grant))
            }
        }
    }
}

@Composable
fun LastPage(onEnter: ()->Unit) {
    LaunchedEffect(Unit) {
        onEnter()
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("您已完成初步设置")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeApp21Theme {
        PermissionScreen(emptyList())
    }
}

@Preview(showBackground = true)
@Composable
fun NavButtonsPreview() {
    ComposeApp21Theme {
        NavigationButtons(
            onClickPrevious = {},
            onClickNext = {}
        )
    }
}