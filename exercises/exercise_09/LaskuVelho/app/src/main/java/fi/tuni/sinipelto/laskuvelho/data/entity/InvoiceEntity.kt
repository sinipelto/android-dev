package fi.tuni.sinipelto.laskuvelho.data.entity

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class InvoiceEntity(

    var uid: String? = null, // Item Firestore UID (is manually assigned when the item is retrieved from the db)

    val payee: String? = null, // Name of the invoice receiver (any string value)

    val invoiceType: Int? = null, // Regular integer has sufficient bitcount for invoice type enumeration

    val amount: Double? = null,

    // Using TypeConverter for java.util.Date <-> kotlin.Long -> use timestamp from cloud server time (prevent manipulations)
    @ServerTimestamp val dueDate: Date? = null

) : Serializable