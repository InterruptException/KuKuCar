package com.example.composeapp21

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {
    val speedState = MutableStateFlow(0)
    val runningState = MutableStateFlow(false)
    private var sensorManager : SensorManager? = null
    private val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(sensorEventListener)
        sensorManager = null
    }
    fun startSensor(context: Context) {
        sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
//        sensorManager?.registerListener(sensorEventListener)
    }

    private fun updateSpeed() {
        viewModelScope.launch {

        }
    }

    fun start(context: Context) {}

    fun stop() {}

    fun pause() {}

    fun toggle() {

    }
}