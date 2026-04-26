package com.wenha.cefrenglish.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SimpleViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    override fun <R : ViewModel> create(modelClass: Class<R>): R = create() as R
}
