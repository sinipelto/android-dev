package fi.tuni.sinipelto.laskuvelho.application

import android.app.Application
import fi.tuni.sinipelto.laskuvelho.data.AppDatabase
import fi.tuni.sinipelto.laskuvelho.data.repository.InvoiceRepository

class MainApplication : Application() {
//    val scope = CoroutineScope(SupervisorJob())
    val db by lazy { AppDatabase.getDatabase(this /*scope*/) }
    val repo by lazy { InvoiceRepository(db.invoiceDao()) }
}