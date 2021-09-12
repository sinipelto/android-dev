package fi.tuni.sinipelto.stepcounter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

// Class for creating yes-no, neutral, or non-interactive dialog prompts to the user.
// Allows for either accept-only or both accept/deny dialogs with caller-provided action callbacks
// If no listener is provided for the input (null), the dialog is simply closed when the action is called.
class UserPrompt(

    ctx: Context,
    private val title: String,
    private val message: String,
    private val acceptValue: String?,
    private val acceptCallback: DialogInterface.OnClickListener?,
    private val denyValue: String?,
    private val denyCallback: DialogInterface.OnClickListener?,
    private val dismissCallback: DialogInterface.OnDismissListener?

) : AlertDialog.Builder(ctx) {

    // Set cancellability based on the dialog denial value. If no such provided, cannot cancel
    override fun setCancelable(cancelable: Boolean): AlertDialog.Builder {
        return super.setCancelable(!denyValue.isNullOrBlank())
    }

    // Create the dialog display. Showing the dialog is left on caller responsibility
    // Call .show() to show the dialog
    override fun create(): AlertDialog? {
        this.setTitle(title)
        this.setMessage(message)

        // Both actions (accept/deny) defined
        if (acceptValue != null && denyValue != null) {
            this.setPositiveButton(acceptValue, acceptCallback)
            this.setNegativeButton(denyValue, denyCallback) // denyCallback also NULL
        }
        // Only single action defined (accept)
        else if (denyValue == null) {
            this.setNeutralButton(acceptValue, acceptCallback)
        }
        // else: no actions defined -> must define dismissCallback to close the dialog
        else {
            this.setOnDismissListener(dismissCallback)
        }

        return super.create()
    }

}