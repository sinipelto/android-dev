package fi.tuni.sinipelto.disqs.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.*

class BinaryConverter {
    companion object {

        fun bitmapToBase64(bitmap: Bitmap): String {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            val bytes = stream.toByteArray()
            stream.close()
            return Base64.getEncoder().encodeToString(bytes)
        }

        fun base64ToBitmap(string: String): Bitmap {
            val bytes = Base64.getDecoder().decode(string)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

    }
}