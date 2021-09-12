package fi.tuni.sinipelto.laskuvelho.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.repository.InvoiceRepository
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repo: InvoiceRepository) : ViewModel() {

    val allInvoices: MutableLiveData<List<InvoiceEntity>> = MutableLiveData()

    fun getAll(): MutableLiveData<List<InvoiceEntity>> {
        viewModelScope.launch {
            repo.getAll().addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(
                        DataConstants.LOG_TAG,
                        "ERROR: getAll(): Failed to get invoices from Repo."
                    )
                    allInvoices.value = listOf() // Clear existing values in case of error
                    return@addSnapshotListener
                }

                if (snap != null) {
                    val items: MutableList<InvoiceEntity> = mutableListOf()
                    if (!snap.isEmpty) {
                        for (doc in snap) {
                            val obj = (doc.toObject(InvoiceEntity::class.java))
                            obj.uid = doc.id // Update doc UID ref
                            items.add(obj)
                        }
                    }
                    allInvoices.value = items
                } else {
                    Log.e(DataConstants.LOG_TAG, "ERROR: getAll(): Received snap was null.")
                    allInvoices.value = listOf() // Clear existing values in case of error
                }
            }
        }
        return allInvoices
    }

    fun insert(item: InvoiceEntity) = viewModelScope.launch {
        repo.insert(item)
    }

    fun remove(item: InvoiceEntity) = viewModelScope.launch {
        repo.remove(item)
    }

}