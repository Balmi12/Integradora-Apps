package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var gravity: FloatArray? = null
    private var magnetic: FloatArray? = null

    private lateinit var tvAltitude: TextView
    private lateinit var tvOrientation: TextView
    private lateinit var tvPosition: TextView
    private lateinit var compassView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas
        tvAltitude = findViewById(R.id.tv_altitude)
        tvOrientation = findViewById(R.id.tv_orientation)
        tvPosition = findViewById(R.id.tv_position)
        compassView = findViewById(R.id.compassView)

        // Cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Gestión de sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        checkPermissions()

        // Registramos sensores
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gravitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magneticSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getAltitude()
        }
    }

    private fun getAltitude() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val altitude = location.altitude
                    val latitude = location.latitude
                    val longitude = location.longitude
                    tvAltitude.text = "Altitud: ${altitude.roundToInt()} m"
                    tvPosition.text = "Posición: Lat: $latitude, Lon: $longitude"
                } else {
                    tvPosition.text = "Posición: Sin datos de ubicación"
                }
            }.addOnFailureListener {
                tvPosition.text = "Error al obtener la ubicación"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getAltitude()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravity = it.values
                    updateOrientation()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetic = it.values
                    updateOrientation()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateOrientation() {
        if (gravity != null && magnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, magnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                tvOrientation.text = "Orientación: ${azimuth.roundToInt()}°"

                var adjustedAzimuth = azimuth % 360
                if (adjustedAzimuth < 0) adjustedAzimuth += 360
                val calibratedAzimuth = (adjustedAzimuth + 270) % 360
                compassView.rotation = -calibratedAzimuth
            }
        }
    }
}
