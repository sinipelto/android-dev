package fi.tuni.sinipelto.laskuvelho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.tuni.sinipelto.laskuvelho.data.repository.InvoiceRepository

class InvoiceViewModelFactory(private val repo: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repo) as T
        } else {
            throw ClassCastException("Invalid cast of ViewModel")
        }
    }
}