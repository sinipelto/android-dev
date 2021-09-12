package fi.tuni.sinipelto.laskuvelho.application

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.firestore.FirebaseFirestore
import fi.tuni.sinipelto.laskuvelho.data.AppDatabase
import fi.tuni.sinipelto.laskuvelho.data.model.InvoiceType
import fi.tuni.sinipelto.laskuvelho.data.repository.InvoiceRepository

class MainApplication : Application() {

    private val db by lazy { AppDatabase.getDatabase() }
    val repo by lazy { InvoiceRepository(db) }

    override fun onCreate() {
        super.onCreate()

        // For invoice type class self-test purpose
        @Suppress("UNUSED_VARIABLE")
        val invoiceType = InvoiceType(applicationContext, InvoiceType.Companion.Type.OtherInvoice)

        // Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        @Suppress("DEPRECATION")
        // Facebook app analytics init
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}