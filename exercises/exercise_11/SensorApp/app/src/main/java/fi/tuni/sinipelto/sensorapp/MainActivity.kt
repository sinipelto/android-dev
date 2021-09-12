package fi.tuni.sinipelto.sensorapp

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.MathContext
import java.math.RoundingMode

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "SensorApp.MainActivity"

    private lateinit var sensorMgr: SensorManager
    private lateinit var sensors: Map<Int, Sensor>

    private lateinit var lightSensor: TextView
    private lateinit var proximity: TextView
    private lateinit var gyroscope: TextView
    private lateinit var orientation: TextView
    private lateinit var accele: TextView
    private lateinit var temp: TextView
    private lateinit var pressure: TextView
    private lateinit var rotation: TextView

    private var paused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lightSensor = findViewById(R.id.lightValue)
        proximity = findViewById(R.id.proximityValue)
        gyroscope = findViewById(R.id.gyroscopeValue)
        orientation = findViewById(R.id.orientationValue)
        accele = findViewById(R.id.accelerationValue)
        temp = findViewById(R.id.tempValue)
        pressure = findViewById(R.id.pressureValue)
        rotation = findViewById(R.id.rotationValue)

        findViewById<Button>(R.id.pauseBtn).setOnClickListener {
            if (!paused) {
                (it as Button).text = getString(R.string.pause_button_resume)
                paused = true
                sensorMgr.unregisterListener(this@MainActivity)
            } else if (paused) {
                (it as Button).text = getString(R.string.pause_button_pause)
                registerSensors()
                paused = false
            }
        }

        // Retrieve sensor manager
        sensorMgr = getSystemService(SENSOR_SERVICE) as SensorManager

        // Retrieve system sensors
        sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL).map {
            it.type to it
        }.toMap()

        // Mark null sensors as error
        for (s in sensors) {
            var selected: TextView? = null
            if (sensorMgr.getDefaultSensor(s.value.type) == null) {
                when (s.value.type) {
                    Sensor.TYPE_LIGHT -> {
                        selected = lightSensor
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        selected = accele
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        selected = proximity
                    }
                    Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                        selected = temp
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        selected = gyroscope
                    }
                    Sensor.TYPE_PRESSURE -> {
                        selected = pressure
                    }
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        selected = rotation
                    }
                    Sensor.TYPE_ORIENTATION -> {
                        // 0 1 2
                        selected = orientation
                    }
                }
                selected?.text = getString(R.string.sensor_value_error)
            }
        }
    }

    private fun registerSensors() {
        // Register all (TODO: only selected!!) sensors to listen this activity
        // Use sensible sampling period
        for (sensor in sensors.entries) {
            sensorMgr.registerListener(
                this@MainActivity,
                sensor.value,
                1_000_000
            ) // MICROseconds! (millionth)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!paused) {
            registerSensors()
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister all sensors on activity pause to save battery
        // (app might not get killed instantly => ondestroy is possibly not called immediately)
        sensorMgr.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (paused) return

        if (event?.sensor == null || event.values.isEmpty()) {
            Log.e(TAG, "Event was null or no values.")
            return
        }

        // Set value precisions to show
        if (event.values.count() > 0) {
            event.values[0] =
                event.values[0].toBigDecimal(MathContext(2, RoundingMode.HALF_UP)).toFloat()
        }
        if (event.values.count() > 1) {
            event.values[1] =
                event.values[1].toBigDecimal(MathContext(2, RoundingMode.HALF_UP)).toFloat()
        }
        if (event.values.count() > 2) {
            event.values[2] =
                event.values[2].toBigDecimal(MathContext(2, RoundingMode.HALF_UP)).toFloat()
        }
        if (event.values.count() > 3) {
            event.values[3] =
                event.values[3].toBigDecimal(MathContext(2, RoundingMode.HALF_UP)).toFloat()
        }

        // Catch any tracked sensor and update value on screen
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                // 0
                lightSensor.text = "${event.values[0]}"
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // 0 1 2 => x y z
                accele.text = "x: ${event.values[0]} y: ${event.values[1]} z: ${event.values[2]}"
            }
            Sensor.TYPE_PROXIMITY -> {
                // 0
                proximity.text = "${event.values[0]}"
            }
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                // 0
                temp.text = "${event.values[0]}"
            }
            Sensor.TYPE_GYROSCOPE -> {
                // 0 1 2
                gyroscope.text = "x: ${event.values[0]} y: ${event.values[1]} z: ${event.values[2]}"
            }
            Sensor.TYPE_PRESSURE -> {
                // 0
                pressure.text = "${event.values[0]}"
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                // 0 1 2 3
                rotation.text =
                    "x: ${event.values[0]} y: ${event.values[1]} z: ${event.values[2]} cos: ${event.values[3]}"
            }
            Sensor.TYPE_ORIENTATION -> {
                // 0 1 2
                orientation.text =
                    "x: ${event.values[0]} y: ${event.values[1]} z: ${event.values[2]}"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (paused) return
        Log.d(TAG, "Accuracy changed: $sensor ACC: $accuracy")
    }
}