    package com.example.maps

    import android.Manifest
    import android.content.pm.PackageManager
    import android.hardware.Sensor
    import android.hardware.SensorEvent
    import android.hardware.SensorEventListener
    import android.hardware.SensorManager
    import android.location.Location
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import com.google.android.gms.tasks.OnSuccessListener
    import kotlin.math.roundToInt

    class MainActivity : AppCompatActivity(), SensorEventListener {

        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var sensorManager: SensorManager
        private var gravity: FloatArray? = null
        private var magnetic: FloatArray? = null

        private lateinit var tvSteps: TextView
        private lateinit var tvAltitude: TextView
        private lateinit var tvOrientation: TextView
        private lateinit var tvPosition: TextView
        private lateinit var compassView: ImageView // Imagen de la brújula
        private lateinit var resetButton: Button

        private var currentStepCount = 0
        private var stepsAtReset = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // Inicialización de vistas
            tvSteps = findViewById(R.id.tv_steps)
            tvAltitude = findViewById(R.id.tv_altitude)
            tvOrientation = findViewById(R.id.tv_orientation)
            tvPosition = findViewById(R.id.tv_position)
            compassView = findViewById(R.id.compassView) // Vista de la brújula
            resetButton = findViewById(R.id.resetButton) // Botón para reiniciar los pasos

            // Inicialización del cliente de ubicación
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Inicialización del gestor de sensores
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

            // Configuración del botón de reinicio
            resetButton.setOnClickListener {
                stepsAtReset = currentStepCount // Guardar el valor de pasos al reiniciar
                updateStepCount() // Actualizar el contador de pasos
            }

            // Verificar permisos para acceder a la ubicación
            checkPermissions()

            // Registramos los sensores de gravedad y magnetómetro para obtener la orientación
            val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            gravitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magneticSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }

        private fun checkPermissions() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        // Método para obtener la altitud usando el GPS (a través de FusedLocationProviderClient)
        private fun getAltitude() {
            // Verifica si tenemos permisos para acceder a la ubicación
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    if (location != null) {
                        // Obtener la altitud en metros
                        val altitude = location.altitude
                        val latitude = location.latitude
                        val longitude = location.longitude

                        tvAltitude.text = "Altitud: ${altitude.roundToInt()} m"
                        tvPosition.text = "Posición: Lat: $latitude, Lon: $longitude"
                    }
                })
            }
        }

        override fun onResume() {
            super.onResume()
            // Llamamos a la función para obtener la altitud
            getAltitude()
        }

        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(this)
        }

        private fun updateStepCount() {
            // Calcula el número de pasos desde el último reinicio y lo muestra en el TextView
            val stepsSinceReset = currentStepCount - stepsAtReset
            tvSteps.text = "Pasos: $stepsSinceReset"
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

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No es necesario manejar cambios de precisión en este caso
        }

        private fun updateOrientation() {
            if (gravity != null && magnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, magnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)

                    // Convertimos la orientación en grados
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                    // Actualizamos la orientación en el TextView
                    tvOrientation.text = "Orientación: ${azimuth.roundToInt()}°"

                    // Aseguramos que el azimut esté dentro del rango de 0 a 360 grados
                    var adjustedAzimuth = azimuth % 360
                    if (adjustedAzimuth < 0) adjustedAzimuth += 360

                    // Calibrar la brújula: Queremos que 90 grados sea hacia arriba
                    // El azimut de 90 grados debe estar en la parte superior de la pantalla (flecha hacia arriba).
                    val calibratedAzimuth = (adjustedAzimuth + 270) % 360

                    // Actualizamos la rotación de la brújula con el valor calibrado
                    compassView.rotation = -calibratedAzimuth
                }
            }
        }


    }
