package fi.tuni.sinipelto.laskuvelho.viewholder

import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import fi.tuni.sinipelto.laskuvelho.R
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.model.DateFormatter
import fi.tuni.sinipelto.laskuvelho.data.model.InvoiceType
import java.math.RoundingMode

class InvoiceViewHolder(val viewToHold: View) :
    RecyclerView.ViewHolder(viewToHold), EventListener<QuerySnapshot> {

    private val invoiceItemView: TextView = itemView.findViewById(R.id.textView)
    val invoiceRemoveBtn: Button = itemView.findViewById(R.id.delBtn)

    fun bind(item: InvoiceEntity) {
        var txt = "Saaja: ${item.payee}" +
                "\n" +
                "Tyyppi: ${InvoiceType(viewToHold.context, InvoiceType.getInvoiceIdByTypeCode(item.invoiceType!!))}" +
                "\n" +
                "Määrä: ${item.amount!!.toBigDecimal().setScale(2, RoundingMode.HALF_UP)} EUR" +
                "\n" +
                "Eräpäivä: "
        val due = DateFormatter.getLocalDateString(item.dueDate!!)

        val len = txt.length
        txt += due

        val todayMidnight = DateFormatter.getTodayMidnight()

        // Check if due date already past -> mark with different color
        when {
            item.dueDate.before(todayMidnight) -> { // date before Today 00:00.0000 (E.g. Yesterday 23:59:59.9999)
                val span = Spannable.Factory().newSpannable(txt)
                span.setSpan(
                    ForegroundColorSpan(DataConstants.DANGER_TEXT_COLOR),
                    len,
                    len + due.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                invoiceItemView.text = span
            }
            DateFormatter.getLocalDateMidnight(item.dueDate) == todayMidnight -> {
                val span = Spannable.Factory().newSpannable(txt)
                span.setSpan(
                    ForegroundColorSpan(DataConstants.WARNING_TEXT_COLOR),
                    len,
                    len + due.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                invoiceItemView.text = span
            }
            else -> {
                invoiceItemView.setTextColor(DataConstants.DEFAULT_TEXT_COLOR)
                invoiceItemView.text = txt
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): InvoiceViewHolder {
            Log.d(DataConstants.LOG_TAG, "ViewHolder.create")
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item, parent, false)
            return InvoiceViewHolder(view)
        }
    }

    override fun onEvent(snapshot: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(DataConstants.LOG_TAG, "OnEvent.ERROR", error)
        }

        // Dispatch the event
        for (change in snapshot?.documentChanges!!) {
            // Snapshot of the changed document
            val snap: DocumentSnapshot = change.document
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    Log.d(DataConstants.LOG_TAG, "ADDED: ${snap.data}")
                }
                DocumentChange.Type.MODIFIED -> {
                    Log.d(DataConstants.LOG_TAG, "MODIFIED: ${snap.data}")
                }
                DocumentChange.Type.REMOVED -> {
                    Log.d(DataConstants.LOG_TAG, "REMOVED: ${snap.data}")
                }
            }
        }
    }
}