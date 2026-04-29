package com.wenha.cefrenglish.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SimpleViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    override fun <R : ViewModel> create(modelClass: Class<R>): R {
        val viewModel = create()
        require(modelClass.isAssignableFrom(viewModel::class.java)) {
            "Cannot create ${modelClass.name} from ${viewModel::class.java.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return viewModel as R
    }
}
