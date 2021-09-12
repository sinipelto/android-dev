package fi.tuni.sinipelto.laskuvelho.data.repository

import androidx.annotation.WorkerThread
import fi.tuni.sinipelto.laskuvelho.data.dao.InvoiceDao
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {

    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoicesByDueAsc()

    @WorkerThread
    suspend fun insert(invoice: InvoiceEntity) {
        invoiceDao.insertInvoice(invoice)
    }

    @WorkerThread
    suspend fun remove(invoice: InvoiceEntity) {
        invoiceDao.deleteInvoice(invoice)
    }

}