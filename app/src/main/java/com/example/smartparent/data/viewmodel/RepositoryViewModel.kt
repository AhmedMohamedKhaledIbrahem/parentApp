package com.example.smartparent.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartparent.data.ParentDatabase
import com.example.smartparent.data.ParentRepository


open class RepositoryViewModel(application: Application) : AndroidViewModel(application) {
    protected val repository: ParentRepository

    init {
        val parentDao = ParentDatabase.getDataBaseInstance(application).parentDao()
        repository = ParentRepository(parentDao)
    }

}