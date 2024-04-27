package com.example.smartparent.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("ConnectedDeviceContact")
data class ConnectedDeviceContactModel(
    @PrimaryKey(autoGenerate = true)
    val id :Int = 0,
    val personId :String = "",
    val personEmail:String = "",
    val blindId:String = "",
    val blindEmail:String = "",
    val connected :Int = 0
    ){
    constructor():this(0,"","","","",0)
}
