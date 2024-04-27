package com.example.smartparent.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartparent.data.model.ConnectedDeviceContactModel
import com.example.smartparent.data.model.UserProfileModel

@Dao
interface ParentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserProfile(userProfile: UserProfileModel)

    @Query("Select * from UserProfile")
    fun getUserProfile(): LiveData<List<UserProfileModel>>

    @Query("Select * from UserProfile")
    fun getUserProfileSingleData(): LiveData<UserProfileModel>

    @Query("Delete From UserProfile")
    fun deleteAll()





    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConnectedDeviceContact(connectedDeviceContactModel: ConnectedDeviceContactModel)

    @Query("select * from ConnectedDeviceContact")
    fun getConnectedDeviceContact(): LiveData<List<ConnectedDeviceContactModel>>

    @Query("delete  from ConnectedDeviceContact")
    fun deleteAllConnectedDeviceContact()


}