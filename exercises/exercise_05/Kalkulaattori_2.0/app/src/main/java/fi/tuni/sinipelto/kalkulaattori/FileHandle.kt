package fi.tuni.sinipelto.kalkulaattori

import android.util.Log
import java.io.File
import java.io.IOException

// Used for ensuring that file can be at least read or written, or both
enum class FileMode {
    Read,
    Write,
    ReadWrite
}

// This is not a proper way. Using file streams would be more efficient with larger files
// However, it works for this small learning scenario, where file size tend to be small
class FileHandler {
    private val tag = "KALKULAATTORI.FILEHANDLER"

    var file: File? = null
    var content: String? = null

    fun open(path: String, mode: FileMode) {
        if (file != null) {
            Log.e(tag, "File already open. Close it first.")
            return
        }

        file = File(path)
        file!!.createNewFile() // If and only if the file does not exist

        if ((mode == FileMode.Read || mode == FileMode.ReadWrite) && !file!!.canRead()) {
            val err = "Could not open file $path with with read access."
            Log.e(tag, err)
            throw IOException("$tag: $err")
        }
        if ((mode == FileMode.Write || mode == FileMode.ReadWrite) && !file!!.canWrite()) {
            val err = "Could not open file $path with write access."
            Log.e(tag, err)
            throw IOException("$tag: $err")
        }

        Log.d(tag, "File opened in path: $path")
        read() // Read the current file contents to buffer
    }

    fun close() {
        // Free file resources
        file = null
        content = null
    }

    fun write(data: String) {
        if (file == null) {
            Log.e(tag, "File not open. Open it first.")
            throw IOException("File not open.")
        }

        content = data
        file!!.writeText(content as String)
    }

    fun append(data: String) {
        if (file == null) {
            Log.e(tag, "File not open. Open it first.")
            throw IOException("File not open.")
        }

        Log.d(tag, "Content before write: $content")
        content += data
        Log.d(tag, "Content after write: $content")
        file!!.writeText(content as String)
    }

    fun read(): String {
        if (file == null) {
            Log.e(tag, "File not open. Open it first.")
            throw IOException("File not open.")
        }

        Log.d(tag, "Content before read: $content")
        content = file!!.readText()
        Log.d(tag, "Content read from file: $content")
        return content as String
    }

    fun isOpen() = file != null
}