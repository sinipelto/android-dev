package fi.tuni.sinipelto.laskuvelho.adapter

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import fi.tuni.sinipelto.laskuvelho.R
import fi.tuni.sinipelto.laskuvelho.data.comparator.InvoiceComparator
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.ui.dialog.RemoveDialog
import fi.tuni.sinipelto.laskuvelho.viewholder.InvoiceViewHolder
import fi.tuni.sinipelto.laskuvelho.viewmodel.InvoiceViewModel

class InvoiceListAdapter(private val viewModel: InvoiceViewModel) :
    ListAdapter<InvoiceEntity, InvoiceViewHolder>(InvoiceComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        return InvoiceViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.invoiceRemoveBtn.setOnClickListener {
            Log.d(
                DataConstants.LOG_TAG,
                "RECYCLER ITEM DELETE BUTTON CLICKED for: ${item.invoiceId}"
            )
            RemoveDialog(holder.viewToHold.context) {
                viewModel.remove(item)
                Toast.makeText(
                    holder.viewToHold.context,
                    holder.viewToHold.context.getString(R.string.remove_success),
                    Toast.LENGTH_LONG
                ).show()
            }.create().show()
        }
    }
}