package fi.tuni.sinipelto.kalkulaattori

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {
    private lateinit var logView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        Log.d(tag, "ONCREATE: ACTIVITY CREATED.")

        logView = findViewById(R.id.view)
        updateView()
        logView.movementMethod = ScrollingMovementMethod()
    }

    private fun updateView()
    {
        logView.text = MainActivity.HistoryFile.read()
    }

    fun clearLog(view: View)
    {
        MainActivity.HistoryFile.write("")
        updateView()
        Log.d(tag, "CLEARLOG: Calculation log cleared.")
    }

    fun returnToMainView(view: View)
    {
        Log.d(tag, "RETURNTOMAINVIEW: FINISHING UP")
        finish()
    }

    private val tag = "KALKULAATTORI.ACTIVITY.LOGVIEW"
}