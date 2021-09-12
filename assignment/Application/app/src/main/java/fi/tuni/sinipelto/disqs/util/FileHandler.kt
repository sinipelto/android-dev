package fi.tuni.sinipelto.disqs.util

import android.content.Context
import java.io.*

class FileHandler {
    companion object {
        fun readAppFile(context: Context, path: String): String {
            val stream = context.openFileInput(path)
            val reader = InputStreamReader(stream)
            val buffered = BufferedReader(reader)
            val text = buffered.readText()
            buffered.close()
            reader.close()
            stream.close()
            return text
        }

        fun writeAppFile(context: Context, path: String, content: String) {
            val stream = context.openFileOutput(path, Context.MODE_PRIVATE)
            val writer = OutputStreamWriter(stream)
            val buffered = BufferedWriter(writer)
            buffered.write(content)
            buffered.close()
            writer.close()
            stream.close()
        }

        fun fileExists(context: Context, fileName: String): Boolean {
            return File(context.filesDir, fileName).exists()
        }
    }
}