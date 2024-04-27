package com.example.smartparent.data.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.smartparent.data.model.ConnectedDeviceContactModel

class ConnectedDeviceContactViewModel(application: Application) : RepositoryViewModel(application) {

    fun insertConnectedDeviceContact(connectedDeviceContactModel: ConnectedDeviceContactModel) {
        repository.insertConnectedDeviceContact(connectedDeviceContactModel)
    }

    fun getConnectedDeviceContact(): LiveData<List<ConnectedDeviceContactModel>> {
        return repository.getConnectedDeviceContact()
    }

    fun deleteAllConnectedDeviceContact() {
        repository.deleteAllConnectedDeviceContact()
    }
}