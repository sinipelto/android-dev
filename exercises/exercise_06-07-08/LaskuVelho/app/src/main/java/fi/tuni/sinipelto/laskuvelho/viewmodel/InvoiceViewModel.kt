package fi.tuni.sinipelto.laskuvelho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.repository.InvoiceRepository
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repo: InvoiceRepository) : ViewModel() {

    val allInvoices: LiveData<List<InvoiceEntity>> = repo.allInvoices.asLiveData()

    fun insert(item: InvoiceEntity) = viewModelScope.launch {
        repo.insert(item)
    }

    fun remove(item: InvoiceEntity) = viewModelScope.launch {
        repo.remove(item)
    }
}