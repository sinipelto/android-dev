package fi.tuni.sinipelto.laskuvelho.ui.dialog

import android.app.AlertDialog
import android.content.Context
import fi.tuni.sinipelto.laskuvelho.R

class RemoveDialog(private val ctx: Context, private val onAccept: () -> Unit) :
    AlertDialog.Builder(ctx) {

    private val title: String = ctx.getString(R.string.dialog_title)
    private val message: String = ctx.getString(R.string.dialog_msg)

    override fun create(): AlertDialog {
        super.setTitle(title)
        super.setMessage(message)
        super.setPositiveButton(ctx.getString(R.string.dialog_yes)) { dialog, _ ->
            run {
                // TODO: Remove item from DB + view
                onAccept()
                dialog.dismiss()
            }
        }
        super.setNegativeButton(ctx.getString(R.string.dialog_no)) { dialog, _ ->
            run {
                dialog.cancel()
            }
        }
        return super.create()
    }
}