package fi.tuni.sinipelto.laskuvelho.data.dao

import androidx.room.*
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY due_date ASC")
    fun getAllInvoicesByDueAsc(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY due_date DESC")
    fun getAllInvoicesByDueDesc(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoice_type = :invoiceType")
    fun getAllInvoicesByType(invoiceType: Int): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoice_type = :invoiceType ORDER BY due_date DESC")
    fun getAllInvoicesByTypeOrderByDueDesc(invoiceType: Int): Flow<List<InvoiceEntity>>

    // We do not tolerate duplicate entries -> unique autogen id ensures no duplicate entries
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoices(invoices: Collection<InvoiceEntity>)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()
}