package com.example.composeapp21

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowColumnOverflow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composeapp21.ui.theme.ComposeApp21Theme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val vm by viewModels<DashboardViewModel>()

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vm.initViewModel(this)
        setContent {
            ComposeApp21Theme(
                darkTheme = true
            ) {
                val enabled : Boolean by vm.runningState.collectAsState()

                Scaffold(
                    floatingActionButton = {
                        StartButton(enabled = enabled, onClick = { vm.toggle() })
                    },
                    floatingActionButtonPosition = FabPosition.EndOverlay,
                    contentWindowInsets = WindowInsets.safeContent
                ) { contentPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            Portrait {
                                MainBody(vm)
                            }
                        } else if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Landscape {
                                MainBody(vm)
                            }
                        } else {
                            Landscape {
                                MainBody(vm)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
        vm.stop()
    }
}

@Composable
fun MainBody(vm: DashboardViewModel) {
    SpeedMps(isDataValid = { vm.hasSpeedState }, provideSpeed = { vm.formatSpeedMps }, speeding = { vm.speedingState })
    SpeedKmph(isDataValid = { vm.hasSpeedState }, provideSpeed = { vm.formatSpeedKmph }, speeding = { vm.speedingState })
    SatelliteCount(isDataValid = { vm.hasGnssSatelliteCount }, provideSpeed = { vm.formatGnssSatelliteCount })
    Altitude(isDataValid = { vm.hasAltitudeState }, provideSpeed = { vm.formatAltitude })
    MslAltitude(isDataValid = { vm.hasMslAltitudeState }, provideSpeed = { vm.formatMslAltitude })
    SpeedAccuracy(isDataValid = { vm.hasSpeedAccuracyState }, provideSpeed = { vm.formatSpeedAccuracy })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Portrait(content: @Composable ()->Unit) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
        maxItemsInEachRow = 1,
        overflow = FlowRowOverflow.Clip
    ) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Landscape(content: @Composable ()->Unit) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        overflow = FlowRowOverflow.Clip
    ) {
        content()
    }
}

@Composable
fun SpeedMps(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>, speeding: ()->MutableStateFlow<Boolean>) {
    val isSpeeding = speeding().collectAsState()
    StringDataItem(
        label = R.string.speed_label, unit = R.string.speed_unit_mps,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 80.sp, unitFontSize = 20.sp,
        contentColor = if (isSpeeding.value) Color.Red else Color.Green
    )
}

@Composable
fun SpeedKmph(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>, speeding: ()->MutableStateFlow<Boolean>) {
    val isSpeeding = speeding().collectAsState()
    StringDataItem(
        label = R.string.speed_label_kmph, unit = R.string.speed_unit_kmph,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 80.sp, unitFontSize = 20.sp,
        contentColor = if (isSpeeding.value) Color.Red else Color.Green
    )
}

@Composable
fun Altitude(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>) {
    StringDataItem(
        label = R.string.altitude_label, unit = R.string.altitude_unit,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 60.sp, unitFontSize = 20.sp)
}

@Composable
fun MslAltitude(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>) {
    StringDataItem(
        label = R.string.msl_altitude_label, unit = R.string.msl_altitude_unit,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 60.sp, unitFontSize = 20.sp)
}

@Composable
fun SatelliteCount(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>) {
    StringDataItem(
        label = R.string.satellite_count_label, unit = R.string.satellite_count_unit,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 60.sp, unitFontSize = 20.sp)
}

@Composable
fun SpeedAccuracy(isDataValid: () -> MutableStateFlow<Boolean>, provideSpeed: ()->MutableStateFlow<String>){
    StringDataItem(
        label = R.string.speed_accuracy_label, unit = R.string.speed_accuracy_unit,
        isDataValid = isDataValid, provideData = provideSpeed,
        labelFontSize = 20.sp, contentFontSize = 60.sp, unitFontSize = 20.sp)
}



@Composable
fun StringDataItem(
    @StringRes label: Int,
    @StringRes unit: Int,
    isDataValid: ()->MutableStateFlow<Boolean>,
    provideData: ()->MutableStateFlow<String>,
    labelFontSize: TextUnit,
    contentFontSize: TextUnit,
    unitFontSize: TextUnit,
    contentColor: Color = Color.Unspecified
) {
    val data = provideData().collectAsState()
    val isValid = isDataValid().collectAsState()
    Column(
        modifier = Modifier.wrapContentSize(align = Alignment.Center)
            .border(2.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        if (isValid.value) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                Text(text = stringResource(label), fontSize = labelFontSize)
                Text(text = stringResource(unit), fontSize = unitFontSize)
            }
            Text(text = data.value, fontSize = contentFontSize, color = contentColor)
        } else {
            Text(text = stringResource(label), fontSize = labelFontSize)
            Text(text = "__", fontSize = contentFontSize)
        }
    }

}


@Composable
fun StartButton (enabled: Boolean, onClick: ()->Unit){
    FloatingActionButton(onClick = onClick, Modifier
        .height(IntrinsicSize.Min)
        .width(IntrinsicSize.Min)) {
        if (enabled) {
            Icon(painterResource(R.drawable.ic_pause), contentDescription = "pause button", modifier = Modifier.fillMaxSize(0.7f))
        } else {
            Icon(painterResource(R.drawable.ic_play), contentDescription = "stopped", modifier = Modifier.fillMaxSize(0.7f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeApp21Theme {
        StartButton(false, onClick = {})
    }
}

@Composable
fun FileBeanListPages(list: List<String>) {
    val pageDataList = remember(key1 = list) {
        list.chunked(10)
    }


}