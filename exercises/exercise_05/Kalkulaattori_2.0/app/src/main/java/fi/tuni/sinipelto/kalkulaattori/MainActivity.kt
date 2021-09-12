package fi.tuni.sinipelto.kalkulaattori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private val tag = "KALKULAATTORI.ACTIVITY.MAINVIEW"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 1..4) {
            val btnId = resources.getIdentifier("calcBtn$i", "id", packageName)
            val firstId = resources.getIdentifier("numFirst$i", "id", packageName)
            val secondId = resources.getIdentifier("numSecond$i", "id", packageName)
            val resultId = resources.getIdentifier("resultView$i", "id", packageName)

            val btn = findViewById<Button>(btnId)
            val first = findViewById<EditText>(firstId)
            val second = findViewById<EditText>(secondId)
            val result = findViewById<TextView>(resultId)

            btn.setOnClickListener {
                // If both empty, reset result value to default
                if (first.text == null || second.text == null || (first.text.isEmpty() && second.text.isEmpty())) {
                    result.text = resources.getString(R.string.resultText)
                    return@setOnClickListener
                }
                // If only either one empty, remember last result
                if (first.text.isEmpty() || second.text.isEmpty()) {
                    return@setOnClickListener
                }

                val fss = first.text
                    .toString()
                val fsd = fss.toDouble()

                val sns = second.text
                    .toString()
                val snd = sns.toDouble()

                var op: Char? = null
                when (i) {
                    1 -> {
                        result.text = MathTools.sum(fsd, snd).toPlainString()
                        op = '+'
                    }
                    2 -> {
                        result.text = MathTools.ret(fsd, snd).toPlainString()
                        op = '-'
                    }
                    3 -> {
                        result.text = MathTools.multi(fsd, snd).toPlainString()
                        op = 'x'
                    }
                    4 -> {
                        result.text = MathTools.div(fsd, snd)?.toPlainString() ?: "NaN"
                        op = '/'
                    }
                }
                HistoryFile.append("$fss $op $sns = " + result.text as String? + "\n")
            }
        }

        // Open file in R/W to store calc operations
        HistoryFile.open(
            applicationContext.filesDir.absolutePath + File.pathSeparator + "calchistory",
            FileMode.ReadWrite
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        HistoryFile.close()
    }

    fun showLogView(view: View) {
        val act = Intent(this, LogActivity::class.java)
        startActivity(act)
    }

    companion object {
        val HistoryFile = FileHandler()
    }
}