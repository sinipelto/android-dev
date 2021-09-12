package fi.tuni.sinipelto.gpsmapapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View

class PermissionDeniedAction(private val context: Context, private val activity: Activity) :
    View.OnClickListener {
    override fun onClick(v: View?) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
}