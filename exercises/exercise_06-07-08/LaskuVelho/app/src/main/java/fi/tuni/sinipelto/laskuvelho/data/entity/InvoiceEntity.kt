package fi.tuni.sinipelto.laskuvelho.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import java.util.*

@Entity(tableName = DataConstants.INVOICE_TABLE_NAME)
data class InvoiceEntity(

    // We might have multiple invoices with same targets, thus
    // separate, unqiue key is needed
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val invoiceId: Long, // Sqlite does not support java.math.BigInteger java type

    @ColumnInfo(name = "payee_name") val payee: String,

    @ColumnInfo(name = "invoice_type") val invoiceType: Int, // Regular integer has sufficient bitcount for invoice type enumeration

    @ColumnInfo(name = "amount") val amount: Double, // Sqlite doesnt support java.math.BigDecimal java type

    @ColumnInfo(name = "due_date") val dueDate: Date // Using TypeConverter for java.util.Date <-> kotlin.Long

)
