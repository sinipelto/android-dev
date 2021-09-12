package fi.tuni.sinipelto.disqs.util

import android.graphics.Color
import android.widget.Button

class ButtonManager {
    companion object {
        fun setButtonState(button: Button, enabled: Boolean) {
            if (enabled) {
                button.isClickable = true
                button.setBackgroundColor(Color.MAGENTA)
            } else {
                button.isClickable = false
                button.setBackgroundColor(Color.GRAY)
            }
        }
    }
}