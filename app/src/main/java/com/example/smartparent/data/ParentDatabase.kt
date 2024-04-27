package com.example.smartparent.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartparent.data.model.ConnectedDeviceContactModel
import com.example.smartparent.data.model.UserProfileModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(entities = [UserProfileModel::class,ConnectedDeviceContactModel::class], version = 14)
abstract class ParentDatabase : RoomDatabase() {
    abstract fun parentDao(): ParentDao

    companion object {
        @Volatile
        var instance: ParentDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDataBaseInstance(context: Context): ParentDatabase {
            val tempInstance = instance
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val roomDataBaseInstance = Room.databaseBuilder(
                    context,
                    ParentDatabase::class.java,
                    "Parent"
                ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                instance = roomDataBaseInstance
                return roomDataBaseInstance
            }
        }
    }

}