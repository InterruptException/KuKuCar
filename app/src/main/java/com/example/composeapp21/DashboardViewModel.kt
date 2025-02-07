package com.example.composeapp21

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.location.GnssStatusCompat
import androidx.core.location.LocationCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.os.HandlerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.NumberFormat

class DashboardViewModel: ViewModel() {
    //数字格式化
    //保留1位小数
    val nf1 = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
        roundingMode = RoundingMode.HALF_UP
    }
    //保留2位小数
    val nf2 = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 2
        roundingMode = RoundingMode.HALF_UP
    }

    //速度
    val hasSpeedState = MutableStateFlow(false)
    val speedState = MutableStateFlow(0f)
    val formatSpeedMps = MutableStateFlow("")
    val formatSpeedKmph = MutableStateFlow("")
    val speedingState = MutableStateFlow(false)


    //速度精度
    val hasSpeedAccuracyState = MutableStateFlow(false)
    val speedAccuracyState = MutableStateFlow(0f)
    val formatSpeedAccuracy = MutableStateFlow("")

    //平均海拔
    val hasMslAltitudeState = MutableStateFlow(false)
    val mslAltitudeState = MutableStateFlow(0.0)
    val formatMslAltitude = MutableStateFlow("")

    //平均海拔精度
    val hasMslAltitudeAccuracyState = MutableStateFlow(false)
    val mslAltitudeAccuracyState = MutableStateFlow(0f)

    //海拔
    val hasAltitudeState = MutableStateFlow(false)
    val altitudeState = MutableStateFlow(0.0)
    val formatAltitude = MutableStateFlow("")

    val latitudeState = MutableStateFlow(0.0)  //维度
    val longitudeState = MutableStateFlow(0.0) //经度

    //方位角
    val hasBearingState = MutableStateFlow(false)
    val bearingState = MutableStateFlow(0.0f) //方位角

    //方位角精度
    val hasBearingAccuracyState = MutableStateFlow(false)
    val bearingAccuracyState = MutableStateFlow(0.0f)

    val runningState = MutableStateFlow(false)

    //卫星数
    val hasGnssSatelliteCount = MutableStateFlow(false)
    val gnssSatelliteCount = MutableStateFlow(0)
    val formatGnssSatelliteCount = MutableStateFlow("")

    private var sensorManager : SensorManager? = null
    private val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    private var locationManager: LocationManager? = null


    private val gnssStatusListener = object : GnssStatusCompat.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
            super.onSatelliteStatusChanged(status)
            // 可以搜索到的卫星总数
            val satelliteCount = status.satelliteCount

            hasGnssSatelliteCount.tryEmit(true)
            gnssSatelliteCount.tryEmit(satelliteCount)

//            for (index in 0 until satelliteCount) {
//                // 每个卫星的载波噪声密度
//                val cn0DbHz = status.getCn0DbHz(index)
//                val svid = status.getSvid(index)
//            }
        }
    }

    private val locationListener = object : LocationListenerCompat {
        override fun onLocationChanged(location: Location) {
            hasSpeedState.tryEmit(location.hasSpeed())
            if (location.hasSpeed()) {
                speedState.tryEmit(location.speed)
            }

            hasSpeedAccuracyState.tryEmit(LocationCompat.hasSpeedAccuracy(location))
            if (hasSpeedAccuracyState.value) {
                speedAccuracyState.tryEmit(LocationCompat.getSpeedAccuracyMetersPerSecond(location))
            }

            hasMslAltitudeState.tryEmit(LocationCompat.hasMslAltitude(location))
            if(hasMslAltitudeState.value) {
                mslAltitudeState.tryEmit(LocationCompat.getMslAltitudeMeters(location))
            }
            Log.i("MSL", "hasMslAltitudeState = ${hasMslAltitudeState.value}")

            hasMslAltitudeAccuracyState.tryEmit(LocationCompat.hasMslAltitudeAccuracy(location))
            if (hasMslAltitudeAccuracyState.value) {
                mslAltitudeAccuracyState.tryEmit(
                    LocationCompat.getMslAltitudeAccuracyMeters(location)
                )
            }

            hasAltitudeState.tryEmit(location.hasAltitude())
            if (location.hasAltitude()) {
                altitudeState.tryEmit(location.altitude)
            }

            latitudeState.tryEmit(location.latitude)    //纬度
            longitudeState.tryEmit(location.longitude)  //经度

            hasBearingState.tryEmit(location.hasBearing())
            if (hasSpeedState.value) {
                bearingState.tryEmit(location.bearing)
            }

            hasBearingAccuracyState.tryEmit(LocationCompat.hasBearingAccuracy(location))
            if (hasBearingAccuracyState.value) {
                bearingAccuracyState.tryEmit(LocationCompat.getBearingAccuracyDegrees(location))
            }

        }
    }

    private lateinit var mAppContext: Context

    fun initViewModel(context: Context) {
        mAppContext = context.applicationContext

        initFormatters()

        viewModelScope.launch {
            runningState.collect{ isRunning->
                if (isRunning) {
                    startAll()
                } else {
                    stopAll()
                }
            }
        }


    }

    private fun <T> collect(flow: Flow<T>, action: suspend (T)->Unit){
        viewModelScope.launch {
            flow.collect {
                action(it)
            }
        }
    }

    private fun initFormatters() {
        collect(speedState){
            formatSpeedMps.emit(nf1.format(it))
            formatSpeedKmph.emit(nf1.format(it* 3.6)) //  1 m/s = 3.6km/h
            speedingState.emit(it * 3.6 > 120)
        }
        collect(altitudeState){
            formatAltitude.emit(nf1.format(it))
        }

        collect(gnssSatelliteCount){
            if (runningState.value) {
                formatGnssSatelliteCount.emit("$it")
            } else {
                formatGnssSatelliteCount.emit("__")
            }
        }
        collect(speedAccuracyState){
            if (hasSpeedAccuracyState.value) {
                formatSpeedAccuracy.emit(nf1.format(it))
            } else {
                formatSpeedAccuracy.emit("N/A")
            }
        }

        collect(mslAltitudeState){
            if (hasMslAltitudeState.value) {
                formatMslAltitude.emit(nf1.format(it))
            } else {
                formatMslAltitude.emit("N/A")
            }
        }
    }

    private fun stopSensor() {
        sensorManager?.unregisterListener(sensorEventListener)
        sensorManager = null
    }

    private fun startSensor(appContext: Context) {
        sensorManager = appContext.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
//        sensorManager?.registerListener(sensorEventListener)
    }

    @SuppressLint("MissingPermission")
    private fun initGnss(context: Context) {
        val locMgr = getLocationManager(context)?:return

        val locReq = LocationRequestCompat.Builder(500)
            .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
            .setIntervalMillis(500)
            .build()

        LocationManagerCompat.requestLocationUpdates(locMgr,
            LocationManager.GPS_PROVIDER,
            locReq,
            locationListener,
            Looper.getMainLooper()
        )

        LocationManagerCompat.registerGnssStatusCallback(locMgr,
            gnssStatusListener,
            HandlerCompat.createAsync(Looper.getMainLooper())
        )
    }

    private fun getLocationManager(context: Context): LocationManager? {
        if (locationManager == null) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
        if (locationManager == null) {
            Log.w("DashboardViewModel", "无法获取位置服务")
        }
        return locationManager
    }

    private fun startAll() {
        initGnss(mAppContext)
    }

    @SuppressLint("MissingPermission")
    private fun stopGnss(context: Context) {
        val locMgr = getLocationManager(context)?:return
        LocationManagerCompat.unregisterGnssStatusCallback(locMgr, gnssStatusListener)
        LocationManagerCompat.removeUpdates(locMgr, locationListener)
    }

    private fun stopAll() {
        stopGnss(mAppContext)
    }


    fun start() {
        viewModelScope.launch {
            runningState.emit(true)
        }
    }

    fun stop() {
        viewModelScope.launch {
            runningState.emit(false)
        }
    }

    fun toggle() {
        if (runningState.value) {
            stop()
        } else {
            start()
        }
//        viewModelScope.launch {
//            runningState.emit(!runningState.value)
//        }
    }
}