package fi.tuni.sinipelto.stepcounter

import android.content.Context
import android.content.DialogInterface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), SensorEventListener, PermissionListener {

    private val TAG = "STEPCOUNTER.MAINACTIVITY.LOGGER"

    // Sensors required for step tracking
    // NOTE!: System services not available to Activities before onCreate()
    // => late init in oncreate
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>

    // Manager class for handling step counter saves and loads
    private lateinit var saveManager: FileManager

    // Global state for step counter paused or running
    private var running: Boolean = false
    private var hasPermission: Boolean = false

    // UI display for displaying current steps
    private lateinit var startButton: Button
    private lateinit var counter: TextView

    // Current steps loaded into memory
    private var steps = 0

    // Previous value from counter sensor is stored here (to calculate diff)
    private var previous = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "ONCREATE")

        // Basic initializations first
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ui element for showing step count
        counter = findViewById(R.id.counterValue)

        // Initialize save manager for loading and saving step data
        saveManager = FileManager(this@MainActivity, "stepcount")

        // Load sensor configuration and services
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorList = collectSensors()

        // Do we have the required sensor(s) available?
        val available: Boolean

        // Sanity check that we have proper sensors available
        // If not, prompt user that cannot launch the app and quit
        when {
            sensorList.isEmpty() -> {
                Log.d(TAG, "SensorList empty.")
                available = false
            }
            // Loop through sensors and ensure at least one is functional
            sensorList.all { false } -> {
                Log.d(TAG, "SensorList all sensors null.")
                available = false
            }
            // Sensor available for use
            else -> {
                Log.d(TAG, "Sensor ok.")
                available = true
            }
        }

        Log.d(TAG, "Sensor available: $available")

        // If we do not have sensor available, no point to continue ui creation
        // If no required sensor available, prompt to inform user and force to quit the app on any situation
        if (!available) {
            Log.e(TAG, "ERROR: Failed to activate sensor. Abort oncreate.")
            UserPrompt(
                this,
                "Askelmittaus ei saatavilla",
                "Askelmittarin tarvitsemia sensoreita ei tunnistettu tässä laitteessa. Lataa sovellus sellaiselle laitteelle, jossa on askeltunnistin saatavilla.",
                "Sulje sovellus",
                { _: DialogInterface, _: Int -> exitApp() },
                null,
                null,
                { exitApp() }
            ).show()
            return
        }

        // Collect currently saved step count if available
        updateCounter(saveManager.load()?.toInt() ?: 0)
        Log.d(TAG, "Stepcount read from file: $steps")

        // Bind start-stop button listener to control app global run/pause state
        startButton = findViewById(R.id.startPauseButton)
        startButton.setOnClickListener {
            // If running -> stop receiving data from sensor
            if (running) {
                // Stop listening step sensor and switch button label
                stop()
            }
            // If not running -> resume listening sensor data
            else {
                // Start listening sensor data
                start()
            }
        }

        // Set listener for reset button -> prompt user and reset step count on confirmation
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            UserPrompt(
                this,
                "Nollaa askelmittarin lukema",
                "Ootko nyt ihan varma?",
                "Joo",
                { _: DialogInterface, _: Int -> resetCounter() },
                "No en",
                null,
                {}
            ).show()
        }

        // Set listener for save button -> saves current stepcount to file if button pressed
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveManager.save(steps.toString())
            createSnackbar(
                "Askelmittarin arvo tallennettu tiedostoon.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        ensurePermissions()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "ONPAUSE")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ONDESTROY")
    }

    // Start listening the sensor and set app state to running
    private fun start() {
        if (!hasPermission) {
            ensurePermissions()
        } else {
            // Set button text to pause
            startButton.text = getString(R.string.button_pause)
            registerSensor()
            running = true
        }
        createSnackbar("Askelmittari käynnistetty.", Snackbar.LENGTH_SHORT).show()
    }

    // Stop listening the sensor and set app state to not running
    private fun stop() {
        // Set button text to start
        startButton.text = getString(R.string.button_start)
        unregisterSensor()
        running = false
        createSnackbar("Askelmittari pysäytetty.", Snackbar.LENGTH_SHORT).show()
    }

    // Update step counter UI element value
    // Method to centrify value change action to a single point
    // To manage the update process more easily
    // Another alternative would be using a listener for listening any changes to the step
    // counter integer value and to update the ui element value using a callback within the listener
    private fun updateCounter(value: Int) {
        counter.text = value.toString()
    }

    // Handle Post-process activities and force the app to close
    private fun exitApp() {
        exitProcess(0)
    }

    // Resets the current step count stored in memory
    private fun resetCounter() {
        steps = 0
        updateCounter(0)
        Log.d(TAG, "Step count reset.")
        createSnackbar("Askelmittari nollattu.", Snackbar.LENGTH_SHORT).show()
    }

    // Collect all available sensors that qualify for required data production
    private fun collectSensors(): List<Sensor> {
        val list = mutableListOf<Sensor>()
        list.addAll(sensorManager.getSensorList(Sensor.TYPE_STEP_DETECTOR))
        list.addAll(sensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER))
        return list
    }

    // Function to bind the sensor to this listener to receive sensor data
    private fun registerSensor(sensor: Sensor = sensorList.first()) {
        sensorManager.registerListener(
            this@MainActivity,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        Log.d(TAG, "Sensor(s) listener registered.")
    }

    // Function to stop listening the sensor(s) bind to this listener
    // Used when stop action is requested to save power
    private fun unregisterSensor(sensor: Sensor? = null) {
        if (sensor != null) {
            sensorManager.unregisterListener(this@MainActivity, sensor)
            return
        }
        sensorManager.unregisterListener(this@MainActivity)
        Log.d(TAG, "Sensor(s) listener(s) UNregistered.")
    }

    // Collect accuracy changes on sensor data
    // Do nothing except for any possible radical changes detected on the sensor accuracy
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // If not running or data not available, do nothing
        if (!running) return
        sensor ?: return
        Log.d(TAG, "Sensor accuracy changed: ${sensor.name} (${sensor.type}): $accuracy")
    }

    // Catch incoming sensor events to process the received data
    // Collect data values, format and show to the user through UI
    override fun onSensorChanged(event: SensorEvent?) {
        // If not running or data not available, do nothing
        if (!running) return
        event ?: return

        Log.d(
            TAG,
            "Sensor event: $event: sensor: ${event.sensor} acc: ${event.accuracy} time: ${event.timestamp} val: ${
                event.values.joinTo(StringBuffer())
            }"
        )

        // Process sensor event type
        when (event.sensor.type) {
            // If sensor is detector, we can sum up the received value
            Sensor.TYPE_STEP_DETECTOR -> run {
                val value = event.values.firstOrNull()
                if (value == null || value != 1.0f) {
                    Log.e(TAG, "ERROR: Step Detector returned value not 1.0. Was: $value")
                    return@run
                } else {
                    steps += value.toInt()
                }
            }
            // If the sensor is counter, we need to keep track of the step difference
            // during intervals
            Sensor.TYPE_STEP_COUNTER -> run {
                val value = event.values.firstOrNull()
                Log.d(TAG, "Step counter: received value: $value")
                if (value == null) {
                    Log.d(TAG, "ERROR: Received sensor value: null")
                    return@run
                }

                // Compare against previous reading and store difference as current steps
                if (previous <= 0) {
                    previous = value.toInt()
                    return@run
                }

                steps = (previous - value).toInt()
                previous = value.toInt()
            }
        }

        // Finally, update the ui value
        updateCounter(steps)
    }

    private fun createSnackbar(txt: String, len: Int): Snackbar {
        return Snackbar.make(findViewById(android.R.id.content), txt, len)
    }

    // Ensure we have permissions granted to the Activity sensors
    // If not, try to request the permission from the user.
    // If the permission is already explicitly denied by the user, fails with an error
    // Uses dexter library
    private fun ensurePermissions() {
        Dexter
            .withContext(this@MainActivity)
            .withPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
            .withListener(this@MainActivity)
            .withErrorListener {
                Log.e(TAG, "ERROR: Failed to request permissions for activity.")
                createSnackbar(
                    "Pääsy askeltunnistuksen sensoreihin evätty. Anna pääsy sensoreihin sovelluksen asetuksista",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .check()
    }

    // Handle permission granted case
    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        hasPermission = true
    }

    // Handle permission denied case
    // Cannot proceed -> quit the app and notify
    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
        p0 ?: return

        // If deny was not permanent, try asking again
        if (!p0.isPermanentlyDenied) {
            ensurePermissions()
        }
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
    }
}