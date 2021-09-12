package fi.tuni.sinipelto.stepcounter

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

// Class to manage data loads and saves with filesystem files
class FileManager(private val ctx: Context, private val fileName: String) {

    private val TAG = "STEPCOUNTER.SAVEMANAGER.LOGGER"

    // I/O streams for reading and writing the target file
    private lateinit var reader: InputStreamReader
    private lateinit var writer: OutputStreamWriter

    // Save the inserted data into the managed file'
    // Throws any exception occurred during the process (does not permit any type of errors)
    fun save(data: String) {
        Log.d(TAG, "Saving data: '$data' to file: '$fileName'")
        writer = OutputStreamWriter(ctx.openFileOutput(fileName, Context.MODE_PRIVATE))
        writer.write(data)
        writer.flush()
        writer.close()
        Log.d(TAG, "Data saved.")
    }

    // Load string data from file.
    // Returns NULL if file has no data or file does not exist
    fun load(): String? {
        Log.d(TAG, "Reading data from file '$fileName'")
        return try {
            reader = InputStreamReader(ctx.openFileInput(fileName))
            val data = reader.readText()
            Log.d(TAG, "Data read from file: '$data'")
            reader.close()
            if (data.isEmpty() || data.isBlank()) {
                return null
            }
            return data
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, "ERROR: ${ex.message} $ex")
            Log.d(TAG, "Target File does not exist.")
            null
        }
    }
}