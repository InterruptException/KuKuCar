package com.example.composeapp21

import android.Manifest
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composeapp21.ui.theme.ComposeApp21Theme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<DashboardViewModel>()
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
        val permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

        }
        val permsList = mutableListOf(
            Manifest.permission.LOCATION_HARDWARE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        permReqLauncher.launch(permsList.toTypedArray())
        setContent {
            ComposeApp21Theme {
                // A surface container using the 'background' color from the theme
                val enabled : Boolean by vm.runningState.collectAsState()

                Scaffold(floatingActionButton = {
                    StartButton(enabled = enabled, onClick = { vm.toggle() })
                }, floatingActionButtonPosition = FabPosition.EndOverlay) { contentPadding ->
                    Surface(
                        modifier = Modifier.padding(contentPadding).fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Speed("100")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.startSensor(this)
    }

    override fun onPause() {
        super.onPause()
        vm.stopSensor()
    }
}

@Composable
fun Speed(name: String) {
    Text(text = "$name!")
}

@Composable
fun StartButton (enabled: Boolean, onClick: ()->Unit){
    IconButton(onClick = onClick, modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
        if (enabled) {
            Icon(painterResource(R.drawable.ic_pause), contentDescription = "pause button")
        } else {
            Icon(painterResource(R.drawable.ic_play), contentDescription = "stopped")
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