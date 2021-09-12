package fi.tuni.sinipelto.laskuvelho.data.comparator

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity

class InvoiceComparator : DiffUtil.ItemCallback<InvoiceEntity>() {
    override fun areItemsTheSame(oldItem: InvoiceEntity, newItem: InvoiceEntity): Boolean {
        Log.d(DataConstants.LOG_TAG, "Items same: $oldItem === $newItem")
        return oldItem === newItem
    }

    // Check if all fields match, is a duplicate
    override fun areContentsTheSame(oldItem: InvoiceEntity, newItem: InvoiceEntity): Boolean {
        Log.d(
            DataConstants.LOG_TAG,
            "Item contents same: ${oldItem.invoiceId} === ${newItem.invoiceId}"
        )
        return oldItem.invoiceId == newItem.invoiceId
    }
}